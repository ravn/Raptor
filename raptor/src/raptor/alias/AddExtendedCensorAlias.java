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
package raptor.alias;

import org.apache.commons.lang.StringUtils;

import raptor.swt.chat.ChatConsoleController;

public class AddExtendedCensorAlias extends RaptorAlias {
	public AddExtendedCensorAlias() {
		super("+extcensor", "Adds a user to extended censor. ",
				"'+extcensor userName'" + "Example: '+extcensor NewFoundGlory'");
		setHidden(false);
	}

	@Override
	public RaptorAliasResult apply(ChatConsoleController controller,
			String command) {
		if (StringUtils.startsWithIgnoreCase(command, "+extcensor")) {
			if (command.length() < 13) {
				return new RaptorAliasResult(null, "Invalid command: "
						+ command + "\n" + getUsage());
			}
			String whatsLeft = command.substring(10).trim();

			if (whatsLeft.length() < 3) {
				return new RaptorAliasResult(null, "Invalid username: "
						+ whatsLeft + "\n" + getUsage());
			} else if (!controller.getConnector().isOnExtendedCensor(whatsLeft)) {
				controller.getConnector().addExtendedCensor(whatsLeft);
				return new RaptorAliasResult(null, "Added " + whatsLeft
						+ " to extended censor.");
			} else {
				return new RaptorAliasResult(null, whatsLeft
						+ " is already on your extended censor list.");
			}
		} else {
			return null;
		}
	}
}
