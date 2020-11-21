/*
 * Copyright 2020 clocken
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

function togglePlanSelectionForApplink( applink ) {
    AJS.$( "[id*='plan_selection_for_']" ).not( "#plan_selection_for_" + applink ).addClass( "hidden" );
    AJS.$( "#plan_selection_for_" + applink ).removeClass( "hidden" );
    toggleVariableSelectionForPlan( AJS.$( "#selected_plan_for_" + applink ).val() );
}

function toggleVariableSelectionForPlan( plan ) {
    AJS.$( "[id*='variable_selection_for_']" ).not( "#variable_selection_for_" + plan ).addClass( "hidden" );
    AJS.$( "#variable_selection_for_" + plan ).removeClass( "hidden" );
}

function toggleValueSelectionForVariableValueType( variableValueType ) {
    variable = variableValueType.replace(/.*for_/, "");
    if( variableValueType.search("use_field_for") != -1 ) {
        AJS.$( "select#selected_field_for_" + variable ).removeClass( "hidden" );
        AJS.$( "input#custom_value_for_" + variable ).addClass( "hidden" );
    } else {
        AJS.$( "select#selected_field_for_" + variable ).addClass( "hidden" );
        AJS.$( "input#custom_value_for_" + variable ).removeClass( "hidden" );
    }
}