package com.anthem.nimbus.platform.core.process.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

import com.anthem.nimbus.platform.spec.model.dsl.binder.Holder;
import com.anthem.oss.nimbus.core.AbstractTestConfigurer;
import com.anthem.oss.nimbus.core.domain.command.Action;
import com.anthem.oss.nimbus.core.domain.command.Command;
import com.anthem.oss.nimbus.core.domain.command.CommandBuilder;
import com.anthem.oss.nimbus.core.domain.command.CommandMessage;
import com.anthem.oss.nimbus.core.domain.command.execution.CommandExecution.MultiOutput;
import com.anthem.oss.nimbus.core.domain.command.execution.CommandExecution.Output;
import com.anthem.oss.nimbus.core.domain.command.execution.FunctionHandler;
import com.anthem.oss.nimbus.core.domain.model.config.ParamValue;
import com.anthem.oss.nimbus.core.entity.StaticCodeValue;
import com.anthem.oss.nimbus.core.entity.client.Client;
import com.anthem.oss.nimbus.core.entity.queue.MGroupMember;
import com.anthem.oss.nimbus.core.entity.queue.MUser;
import com.anthem.oss.nimbus.core.entity.queue.MUserGroup;
import com.anthem.oss.nimbus.core.entity.queue.Queue;

/**
 * @author Rakesh Patel
 *
 */
@EnableAutoConfiguration
@ActiveProfiles("test")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ParamCodeValueProviderTest extends AbstractTestConfigurer {

	@Autowired
	MongoOperations mongoOps;
	
	
	@Test
	@SuppressWarnings("unchecked")
	public void t1_testSearchByLookupStaticCodeValue() {
		
		CommandMessage cmdMsg = build("Anthem/fep/icr/p/staticCodeValue/_search?fn=lookup&where=staticCodeValue.paramCode.eq('/status')");
		
		MultiOutput multiOp = getCommandGateway().execute(cmdMsg);
		List<Output<?>> ops  = multiOp.getOutputs();
		
		assertNotNull(ops);
		
		List<ParamValue> values = (List<ParamValue>)ops.get(0).getValue(); // TODO having to cast the output, is that correct ??
		assertNotEquals(0, values.size());
		
		values.forEach((v)->System.out.println(v.getCode()));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void t1_testUpdateStaticCodeValue() {
		
		CommandMessage cmdMsg = build("Anthem/fep/icr/p/staticCodeValue/_update");
		
		MultiOutput multiOp = getCommandGateway().execute(cmdMsg);
		List<Output<?>> ops  = multiOp.getOutputs();
		
		assertNotNull(ops);
		
		List<ParamValue> values = (List<ParamValue>)ops.get(0).getValue(); // TODO having to cast the output, is that correct ??
		assertNotEquals(0, values.size());
		
		values.forEach((v)->System.out.println(v.getCode()));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void t2_testSearchByLookupModel() {
		insertClient();
		CommandMessage cmdMsg = build("Anthem/fep/icr/p/client/_search?fn=lookup&projection.mapsTo=code:name,label:name");
		
		//ExecutionContext exContext = new ExecutionContext(cmdMsg, null);
		//List<ParamValue> values = lookupFunctionHandler.execute(exContext, null);
		
		MultiOutput multiOp = getCommandGateway().execute(cmdMsg);
		List<Output<?>> ops  = multiOp.getOutputs();
		
		assertNotNull(ops);
		
		List<ParamValue> values = (List<ParamValue>)ops.get(0).getValue(); // TODO having to cast the output, is that correct ??
		assertNotEquals(0, values.size());
		
		values.forEach((v)->System.out.println(v.getCode()));
	}
	
	@Test
	public void t3_testSearchByExampleCriteriaNull() {
		insertClient();
		CommandMessage cmdMsg = build("Anthem/fep/icr/p/client/_search?fn=example");
//		ExecutionContext exContext = new ExecutionContext(cmdMsg, null);
//		List<?> values = (List<?>)exampleFunctionHandler.execute(exContext, null);
		
		MultiOutput multiOp = getCommandGateway().execute(cmdMsg);
		List<Output<?>> ops  = multiOp.getOutputs();
		
		assertNotNull(ops);
		
		List<?> values = (List<?>)ops.get(0).getValue();
		
		assertNotNull(values);
		assertEquals(2, values.size());
	}
	
	@Test
	public void t4_testSearchByExampleCriteriaNotNull() {
		Client c = insertClient();
		
		CommandMessage cmdMsg = build("Anthem/fep/icr/p/client/_search?fn=example");
		cmdMsg.setRawPayload("{\"code\":\""+c.getCode()+"\"}");
		//ExecutionContext exContext = new ExecutionContext(cmdMsg, null);
		//List<?> values = (List<?>)exampleFunctionHandler.execute(exContext, null);
		
		MultiOutput multiOp = getCommandGateway().execute(cmdMsg);
		List<Output<?>> ops  = multiOp.getOutputs();
		
		assertNotNull(ops);
		
		List<?> values = (List<?>)ops.get(0).getValue();
		
		assertNotNull(values);
		assertEquals(1, values.size());
	}
	
	@Test
	public void t41_testSearchByQueryCriteriaNotNull() {
		Client c = insertClient();
		
		CommandMessage cmdMsg = build("Anthem/fep/icr/p/client/_search?fn=query&where=client.code.eq('c1')");
		
		MultiOutput multiOp = getCommandGateway().execute(cmdMsg);
		List<Output<?>> ops  = multiOp.getOutputs();
		
		assertNotNull(ops);
		
		List<?> values = (List<?>)ops.get(0).getValue();
		
		assertNotNull(values);
		assertEquals(1, values.size());
	}

	@Test
	public void t5_testSearchByQueryWithProjection() {
		CommandMessage cmdMsg = build("Anthem/fep/icr/p/staticCodeValue/_search?fn=query&where=staticCodeValue.paramCode.eq('/status')&projection.alias=vstaticCodeValue");
		//ExecutionContext exContext = new ExecutionContext(cmdMsg, null);
		//List<?> values = (List<?>)queryFunctionHandler.execute(exContext, null);
		
		MultiOutput multiOp = getCommandGateway().execute(cmdMsg);
		List<?> values = (List<?>)multiOp.getSingleResult();
		
		assertNotNull(values);
		assertEquals(1, values.size());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void t6_testSearchByQueryWithCountAggregation() {
		CommandMessage cmdMsg = build("Anthem/fep/icr/p/staticCodeValue/_search?fn=query&where=staticCodeValue.paramCode.eq('/status')&aggregate=count");
//		ExecutionContext exContext = new ExecutionContext(cmdMsg, null);
//		Holder<Long> count = (Holder<Long>)queryFunctionHandler.execute(exContext, null);
		
		MultiOutput multiOp = getCommandGateway().execute(cmdMsg);
		Holder<Long> count = (Holder<Long>)multiOp.getSingleResult();
		
		assertNotNull(count);
		assertEquals(Long.valueOf("1"), count.getState());
	}
	
	@Test
	public void t7_testSearchByQueryAssociation() {
		insertUserAndQueue();
		
		String associationString = getAssociationString();
		
		CommandMessage cmdMsg = build("Anthem/fep/cmapp/p/queue/_search?fn=query&where="+associationString);
		
		//ExecutionContext exContext = new ExecutionContext(cmdMsg, null);
		//List<?> values = (List<?>)queryFunctionHandler.execute(exContext, null);
		
		MultiOutput multiOp = getCommandGateway().execute(cmdMsg);
		List<?> values = (List<?>)multiOp.getSingleResult();
		
		Assert.notEmpty(values, "values cannot be empty");
		values.forEach(System.out::println);
	}
	
	
	@SuppressWarnings("unchecked")
	@Test
	public void t8_testSearchByQueryAssociationWithCountAggregation() {
		insertUserAndQueue();
		
		String associationString = getAssociationString();
		
		CommandMessage cmdMsg = build("Anthem/fep/cmapp/p/queue/_search?fn=query&where="+associationString+"&aggregate=count");
		
		//ExecutionContext exContext = new ExecutionContext(cmdMsg, null);
		//List<Holder<Integer>> values = (List<Holder<Integer>>)queryFunctionHandler.execute(exContext, null);
		
		MultiOutput multiOp = getCommandGateway().execute(cmdMsg);
		List<Holder<Integer>> values = (List<Holder<Integer>>)multiOp.getSingleResult();
		
		Assert.notEmpty(values, "values cannot be empty");
		values.forEach(System.out::println);
		
		assertEquals(Integer.valueOf("1"), values.get(0).getState());
	}

	private void createStaticCodeValues() {
		ParamValue pv = new ParamValue(null,"OPEN","Open");
		ParamValue pv1 = new ParamValue(null, "ACTIVE", "Active");
		
		List<ParamValue> pvs = new ArrayList<>();
		pvs.add(pv);
		pvs.add(pv1);
		
		StaticCodeValue scv = new StaticCodeValue("status", pvs);
		mongoOps.insert(scv, "staticCodeValue");
	}
	
	private Client insertClient() {
		mongoOps.dropCollection("client");
		
		Client c = new Client();
		c.setName("client");
		c.setCode("c");
		mongoOps.insert(c, "client");
		
		Client c1 = new Client();
		c1.setName("client1");
		c1.setCode("c1");
		mongoOps.insert(c1, "client");
		return c;
	}
	
	private void insertUserAndQueue() {
		mongoOps.dropCollection("user");
		mongoOps.dropCollection("queue");
		mongoOps.dropCollection("usergroup");
		mongoOps.dropCollection("groupmember");
		
		Queue queue = new Queue();
		queue.setCode("test1");
		queue.setName("test queue");
		
		MUser user = new MUser();
		user.setName("user1");
		user.setCode("1");
		mongoOps.insert(user,"user");
		
		MGroupMember grpMember = new MGroupMember();
		grpMember.setName("adminMembers");
		grpMember.setAdmin(true);
		grpMember.setUserName("user2");
		mongoOps.insert(grpMember, "groupmember");
		
		MUserGroup ug = new MUserGroup();
		ug.setName("group1");
		ug.addGroupMembers(grpMember);
		mongoOps.insert(ug,"usergroup");
		
		queue.addUserGroups(ug);
		queue.addUsers(user);
		mongoOps.insert(queue);
		
		Queue queue1 = new Queue();
		queue1.setCode("test2");
		queue1.setName("test2 queue");
		MUser user1 = new MUser();
		user1.setName("user2");
		user1.setCode("2");
		mongoOps.insert(user1,"user");
		queue1.addUsers(user1);
		mongoOps.insert(queue1);
	}
	
	private String getAssociationString() {
		return "{\n" + 
				"  \"domainAlias\": \"queue\",\n" + 
				"  \"associatedEntities\": [\n" + 
				"    {\n" + 
				"      \"domainAlias\": \"user\",\n" + 
				"      \"associationStartWith\": \"users\",\n" + 
				"      \"associationFrom\": \"users\",\n" + 
				"      \"associationTo\": \"name\",\n" + 
				"      \"criteria\": [\n" + 
				"        {\n" + 
				"          \"key\": \"name\",\n" + 
				"          \"value\": \"user1\"\n" + 
				"        }\n" + 
				"      ],\n" + 
				"      \"associationAlias\": \"queueToUsers\"\n" + 
				"    },\n" + 
				"    {\n" + 
				"      \"domainAlias\": \"usergroup\",\n" + 
				"      \"associationStartWith\": \"userGroups\",\n" + 
				"      \"associationFrom\": \"userGroups\",\n" + 
				"      \"associationTo\": \"name\",\n" + 
				"      \"criteria\": [],\n" + 
				"      \"associationAlias\": \"queueToGroups\",\n" + 
				"      \"unwind\": true,\n" + 
				"      \"associatedEntities\": [\n" + 
				"        {\n" + 
				"          \"domainAlias\": \"groupmember\",\n" + 
				"          \"associationStartWith\": \"queueToGroups.groupMembers\",\n" + 
				"          \"associationFrom\": \"queueToGroups\",\n" + 
				"          \"associationTo\": \"name\",\n" + 
				"          \"criteria\": [\n" + 
				"            {\n" + 
				"              \"key\": \"userName\",\n" + 
				"              \"value\": \"user1\"\n" + 
				"            }\n" + 
				"          ],\n" + 
				"          \"associationAlias\": \"groupToMember\"\n" + 
				"        }\n" + 
				"      ]\n" + 
				"    }\n" + 
				"  ],\n" + 
				"  \"criteria\": []\n" +
				"}";
	}
	
	private CommandMessage build(String uri) {
		Command cmd = CommandBuilder.withUri(uri).getCommand();
		cmd.setAction(Action._search);
		
		CommandMessage cmdMsg = new CommandMessage();
		cmdMsg.setCommand(cmd);
		return cmdMsg;
	}
}