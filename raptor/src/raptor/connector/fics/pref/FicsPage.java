/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright 2009-2016 RaptorProject (https://github.com/Raptor-Fics-Interface/Raptor)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.connector.fics.pref;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;

import raptor.Raptor;
import raptor.international.L10n;
import raptor.pref.PreferenceKeys;
import raptor.pref.TextFieldEditor;

public class FicsPage extends FieldEditorPreferencePage {
	
	protected static L10n local = L10n.getInstance();
	
	public FicsPage() {
		super(FLAT);
		setTitle("Fics");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		BooleanFieldEditor bfe = new BooleanFieldEditor(
				PreferenceKeys.FICS_AUTO_CONNECT, local.getString("ficsP1"),
				getFieldEditorParent());
		addField(bfe);

//      Timeseal 2 forced for OSX, Timeseal 1 for other OSes.
//      For some reason windows/linux have timeseal2 issues.		
//		addField(new BooleanFieldEditor(PreferenceKeys.FICS_TIMESEAL_IS_TIMESEAL_2,
//				"Use Timeseal 2 (Enables pings through firewalls)", getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.FICS_CLOSE_TABS_ON_DISCONNECT,
				local.getString("ficsP2"), getFieldEditorParent()));

		BooleanFieldEditor bfe2 = new BooleanFieldEditor(
				PreferenceKeys.FICS_KEEP_ALIVE,
				local.getString("ficsP3"),
				getFieldEditorParent());
		addField(bfe2);

		addField(new BooleanFieldEditor(
				PreferenceKeys.FICS_NO_WRAP_ENABLED,
				local.getString("ficsP4"),
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.FICS_SHOW_BUGBUTTONS_ON_PARTNERSHIP,
				local.getString("ficsP5"),
				getFieldEditorParent()));

		addField(new TextFieldEditor(PreferenceKeys.FICS_LOGIN_SCRIPT,
				local.getString("ficsP6"), getFieldEditorParent()));

		addField(new StringFieldEditor(PreferenceKeys.FICS_KEEP_ALIVE_COMMAND,
				local.getString("ficsP7"), getFieldEditorParent()));
		
		addField(new BooleanFieldEditor(PreferenceKeys.FICS_REMOVE_BLANK_LINES,
				local.getString("ficsP8"), getFieldEditorParent()));

	}
}