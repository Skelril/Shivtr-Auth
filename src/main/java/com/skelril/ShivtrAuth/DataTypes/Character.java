/*
 * Copyright (c) 2015 Wyatt Childers.
 *
 * This file is part of Shivtr Auth.
 *
 * Shivtr Auth is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Shivtr Auth is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Shivtr Auth.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.skelril.ShivtrAuth.DataTypes;

/**
 * Author: Turtle9598
 */
public class Character {

    final String playerName;
    String authToken = "";

    public Character(String playerName) {

        this.playerName = playerName.trim();
    }

    public Character(String playerName, String authToken) {

        this.playerName = playerName.trim();
        this.authToken = authToken.trim();
    }

    public String getPlayerName() {

        return playerName.toLowerCase();
    }

    protected String getAuthToken() {

        return authToken;
    }

    public void setAuthToken(String authToken) {

        this.authToken = authToken;
    }
}
