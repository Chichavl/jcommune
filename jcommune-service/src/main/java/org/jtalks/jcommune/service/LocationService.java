/**
 * Copyright (C) 2011  JTalks.org Team
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.jtalks.jcommune.service;

import org.jtalks.jcommune.model.entity.User;

import java.util.Map;

/**
 * class to store user map.
 *
 * @author Andrey Kluev
 */
public interface LocationService {

    /**
     * @return map registry user
     */
    public Map<User, String> getRegisterUserMap();

    /**
     * @param registerUserMap map registry user
     */
    public void setRegisterUserMap(Map<User, String> registerUserMap);
}