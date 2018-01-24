/**
 *  Copyright 2016-2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.anthem.oss.nimbus.test.sample.domain.model.ui;

import com.anthem.oss.nimbus.core.domain.definition.Domain;
import com.anthem.oss.nimbus.core.domain.definition.Domain.ListenerType;
import com.anthem.oss.nimbus.test.sample.domain.model.core.SampleCoreEntity;
import com.antheminc.oss.nimbus.domain.defn.MapsTo;
import com.antheminc.oss.nimbus.domain.defn.Repo;
import com.antheminc.oss.nimbus.domain.defn.Repo.Database;
import com.antheminc.oss.nimbus.domain.defn.ViewConfig.Page;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Soham Chakravarti
 *
 */
@Domain(value="sample_view", includeListeners={ListenerType.websocket})
@MapsTo.Type(SampleCoreEntity.class)
@Repo(Database.rep_none)
@Getter @Setter
public class VRSampleViewRootEntity {

	@Page(route="sample_view_colors")
	private VPSampleViewPageGreen page_green;

	@Page(route="sample_view_colors")
	private VPSampleViewPageBlue page_blue;

	@Page(route="sample_view_colors")
	private VPSampleViewPageRed page_red;

}
