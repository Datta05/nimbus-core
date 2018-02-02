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
package com.anthem.oss.nimbus.test.sample.um.model.view;

import java.time.LocalDate;

import javax.validation.constraints.NotNull;

import com.anthem.oss.nimbus.test.sample.um.model.core.Patient;
import com.anthem.oss.nimbus.test.sample.um.model.core.UMCase;
import com.antheminc.oss.nimbus.domain.defn.MapsTo;
import com.antheminc.oss.nimbus.domain.defn.MapsTo.Path;
import com.antheminc.oss.nimbus.domain.defn.ViewConfig.Button;
import com.antheminc.oss.nimbus.domain.defn.ViewConfig.ComboBox;
import com.antheminc.oss.nimbus.domain.defn.ViewConfig.Hints;
import com.antheminc.oss.nimbus.domain.defn.ViewConfig.Hints.AlignOptions;
import com.antheminc.oss.nimbus.domain.defn.ViewConfig.InputDate;
import com.antheminc.oss.nimbus.domain.defn.ViewConfig.Section;
import com.antheminc.oss.nimbus.domain.defn.ViewConfig.TextBox;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Soham Chakravarti
 *
 */
@MapsTo.Type(UMCase.class)
@Getter @Setter
public class Page_FindPatient {
	

	
	@MapsTo.Type(UMCase.class)
	@Getter @Setter
	public static class Section_CaseInfo  {

		@Path @ComboBox private String requestType;

		@Path @ComboBox private String caseType;
		//@MapsTo.Path private DateRange serviceDate;
    }
	
	

	@MapsTo.Type(value=Patient.class)
	@Getter @Setter
	public static class Form_PatientInfo {

		@Path @NotNull @TextBox private String subscriberId;

		@Path @TextBox private String firstName;

		@Path @TextBox private String lastName;

		@Path @InputDate private LocalDate dob;

		@Button(url="{:id}/_findPatient/_process") @Hints(value=AlignOptions.Left)
		private String action_FindPatient;

		@Button(url="/_nav?a=next") @Hints(value=AlignOptions.Right)
		private String next;
	}
    

	
	@Getter @Setter
	public static class Section_PatientInfo {

		//@Form 
		//@Path(linked=false) private Form_PatientInfo patientInfoEntry;
	}

	
	
	@Section
	private Section_CaseInfo caseInfo;

	@Section
	private Section_PatientInfo patientInfo;

}