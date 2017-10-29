/**
 * 
 */
package com.anthem.oss.nimbus.core.domain.command.execution;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.context.request.RequestContextHolder;

import com.anthem.oss.nimbus.core.BeanResolverStrategy;
import com.anthem.oss.nimbus.core.domain.command.Action;
import com.anthem.oss.nimbus.core.domain.command.Behavior;
import com.anthem.oss.nimbus.core.domain.command.Command;
import com.anthem.oss.nimbus.core.domain.command.CommandMessage;
import com.anthem.oss.nimbus.core.domain.command.execution.CommandExecution.Input;
import com.anthem.oss.nimbus.core.domain.command.execution.CommandExecution.Output;
import com.anthem.oss.nimbus.core.domain.config.builder.DomainConfigBuilder;
import com.anthem.oss.nimbus.core.domain.definition.Repo;
import com.anthem.oss.nimbus.core.domain.model.config.ModelConfig;
import com.anthem.oss.nimbus.core.domain.model.state.QuadModel;
import com.anthem.oss.nimbus.core.domain.model.state.builder.QuadModelBuilder;
import com.anthem.oss.nimbus.core.util.JustLogit;

/**
 * @author Soham Chakravarti
 *
 */
public class DefaultExecutionContextLoader implements ExecutionContextLoader {

	private final DomainConfigBuilder domainConfigBuilder;
	private final CommandExecutor<?> executorActionNew;
	private final CommandExecutor<?> executorActionGet;

	// TODO: Temp impl till Session is rolled out
	private final Map<String, ExecutionContext> sessionCache;
	
	private final QuadModelBuilder quadModelBuilder;
	
	private static final JustLogit logit = new JustLogit(DefaultExecutionContextLoader.class);
	
	public DefaultExecutionContextLoader(BeanResolverStrategy beanResolver) {
		this.domainConfigBuilder = beanResolver.get(DomainConfigBuilder.class);
		this.quadModelBuilder = beanResolver.get(QuadModelBuilder.class);
		
		this.executorActionNew = beanResolver.get(CommandExecutor.class, Action._new.name() + Behavior.$execute.name());
		this.executorActionGet = beanResolver.get(CommandExecutor.class, Action._get.name() + Behavior.$execute.name());
		
		// TODO: Temp impl till Session is rolled out
		this.sessionCache = new HashMap<>(100);
	}
	
	private static String getSessionIdForLogging() {
		final String thSessionId = TH_SESSION.get();
		try {
			String msg = "Session from HTTP: "+ RequestContextHolder.getRequestAttributes().getSessionId()+
							" :: Session  from TH_SESSION: "+ thSessionId;
			return msg;
		} catch (Exception ex) {
			logit.error(()->"Failed to get session info, TH_SESSION: "+thSessionId, ex);
			return "Failed to get session from HTTP, TH_SESSION: "+thSessionId;
		}
	}
	
	@Override
	public final ExecutionContext load(Command rootDomainCmd) {
		logit.trace(()->"[load][I] rootDomainCmd:"+rootDomainCmd+" for "+getSessionIdForLogging());
		
		ExecutionContext eCtx = new ExecutionContext(rootDomainCmd);
		
		// _search: transient - just create shell 
		if(isTransient(rootDomainCmd)) {
			logit.trace(()->"[load] isTransient");
			
			QuadModel<?, ?> q = quadModelBuilder.build(rootDomainCmd);
			eCtx.setQuadModel(q);
			
		} else // _new takes priority
		if(rootDomainCmd.isRootDomainOnly() && rootDomainCmd.getAction()==Action._new) {
			logit.trace(()->"[load] isRootDomainOnly && _new");
			
			eCtx = loadEntity(eCtx, executorActionNew);
			
		} else // check if already exists in session
		if(sessionExists(eCtx)) { 
			logit.trace(()->"[load] sessionExists");
			
			QuadModel<?, ?> q = sessionGet(eCtx);
			eCtx.setQuadModel(q);
			
		} else { // all else requires resurrecting entity
			logit.trace(()->"[load] do _get and put in sessionIfApplicable");
			
			eCtx = loadEntity(eCtx, executorActionGet);
		}
		
		logit.trace(()->"[load][O] rootDomainCmd:"+rootDomainCmd+" for "+getSessionIdForLogging());
		return eCtx;
	}
	
	@Override
	public final void unload(ExecutionContext eCtx) {
		sessionRemomve(eCtx);
		
		// also do an explicit shutdown
		eCtx.getQuadModel().getRoot().getExecutionRuntime().stop();
	}

	private boolean isTransient(Command cmd) {
		return cmd.getAction()==Action._search 
				|| cmd.getAction()==Action._config;
	}
	
	private ExecutionContext loadEntity(ExecutionContext eCtx, CommandExecutor<?> executor) {
		CommandMessage cmdMsg = eCtx.getCommandMessage();
		String inputCmdUri = cmdMsg.getCommand().getAbsoluteUri();
		
		Input input = new Input(inputCmdUri, eCtx, cmdMsg.getCommand().getAction(), Behavior.$execute);
		Output<?> output = executor.execute(input);
		
		// update context
		eCtx = output.getContext();
		
		ModelConfig<?> rootDomainConfig = domainConfigBuilder.getRootDomainOrThrowEx(cmdMsg.getCommand().getRootDomainAlias());
		
		sessionPutIfApplicable(rootDomainConfig, eCtx);
		
		return eCtx;
	}
	
	protected boolean sessionPutIfApplicable(ModelConfig<?> rootDomainConfig, ExecutionContext eCtx) {
		Repo repo = rootDomainConfig.getRepo();
		if(repo==null)
			return false;
		
		if(repo.cache()==Repo.Cache.rep_device) {
			return queuePut(eCtx);
		}

		return false;
	}
	
	protected boolean sessionRemomve(ExecutionContext eCtx) {
		return queueRemove(eCtx);
	}
	
	private void logSessionKeys() {
		logit.trace(()->"session size: "+sessionCache.size());
		
		sessionCache.keySet().stream()
			.forEach(key->logit.trace(()->"session key: "+key));
	}
	
	
	protected boolean sessionExists(ExecutionContext eCtx) {
		return queueExists(eCtx);
	}
	
	protected QuadModel<?, ?> sessionGet(ExecutionContext eCtx) {
		return Optional.ofNullable(queueGet(eCtx))
				.map(ExecutionContext::getQuadModel)
				.orElse(null);
	}
	
	private static final InheritableThreadLocal<String> TH_SESSION = new InheritableThreadLocal<String>() {
		@Override
		protected String initialValue() {
			return RequestContextHolder.getRequestAttributes().getSessionId();
		}
	};
	
	private String getSessionKey(ExecutionContext eCtx) {
		logit.trace(()->"[getSessionKey] eCtx:"+eCtx+" for "+getSessionIdForLogging());
		logSessionKeys();

		String sessionId = TH_SESSION.get();
		String ctxId = eCtx.getId();
		
		String key = ctxId +"_sessionId{"+sessionId+"}";
		return key;
	}
	
	private boolean queueExists(ExecutionContext eCtx) {
		return sessionCache.containsKey(getSessionKey(eCtx));
	}
	
	private ExecutionContext queueGet(ExecutionContext eCtx) {
		return sessionCache.get(getSessionKey(eCtx));
	}
	
	private boolean queuePut(ExecutionContext eCtx) {
		synchronized (sessionCache) {
			sessionCache.put(getSessionKey(eCtx), eCtx);
		}
		return true;
	}

	private boolean queueRemove(ExecutionContext eCtx) {
		// skip if doesn't exist
		if(!queueExists(eCtx))
			return false;
		
		synchronized (sessionCache) {
			ExecutionContext removed = sessionCache.remove(getSessionKey(eCtx));
			return removed!=null;
		}
	}
	
	@Override
	public void clear() {
		synchronized (sessionCache) {
			// shutdown
			sessionCache.values().stream()
				.forEach(e->{
					e.getQuadModel().getRoot().getExecutionRuntime().stop();
				});
			
			// clear cache
			sessionCache.clear();	
		}
	}
}
