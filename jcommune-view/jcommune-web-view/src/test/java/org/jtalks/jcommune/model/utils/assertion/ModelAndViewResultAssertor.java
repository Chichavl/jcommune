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
package org.jtalks.jcommune.model.utils.assertion;

import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.util.Assert;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * @author Mikhail Stryzhonok
 */
public class ModelAndViewResultAssertor<T> implements ResultAssertor<T> {

    private T entityIdentifier;
    private ResultActions actions;
    private ResultMatcher successStatusMatcher;
    private ResultMatcher failStatusMatcher;
    private String successViewName;
    private String failViewName;

    private ModelAndViewResultAssertor(ResultActions actions) {
        this.actions = actions;
    }

    public ModelAndViewResultAssertor withSuccessStatusMatcher(ResultMatcher statusMathcer) {
        this.successStatusMatcher = statusMathcer;
        return this;
    }

    public ModelAndViewResultAssertor withSuccessVeiw(String viewName) {
        this.successViewName = viewName;
        return this;
    }

    public ModelAndViewResultAssertor withFailStatusMatcher(ResultMatcher statusMatcher) {
        this.failStatusMatcher = statusMatcher;
        return this;
    }

    public ModelAndViewResultAssertor withFailView(String viewName) {
        this.failViewName = viewName;
        return this;
    }

    public ModelAndViewResultAssertor withEntityIdentifier(T identifier) {
        this.entityIdentifier = identifier;
        return this;
    }

    public static ModelAndViewResultAssertor fromResultActions(ResultActions actions) {
        return new ModelAndViewResultAssertor(actions);
    }

    @Override
    public T shouldPass() throws Exception {
        Assert.notNull(successStatusMatcher, "To assert successful result successStatusMatcher should be initialized");
        Assert.notNull(successViewName, "To assert successful result successViewName should be initialized");
        actions.andExpect(successStatusMatcher).andExpect(view().name(successViewName));
        return entityIdentifier;
    }

    @Override
    public T shouldFail() throws Exception {
        Assert.notNull(successStatusMatcher, "To assert fail result failStatusMatcher should be initialized");
        Assert.notNull(successViewName, "To assert fail result failViewName should be initialized");
        actions.andExpect(failStatusMatcher).andExpect(view().name(failViewName));
        return entityIdentifier;
    }

    @Override
    public T shouldFailWithAttributeFieldErrors(String name, final String... fieldNames) throws Exception {
        Assert.notNull(successStatusMatcher, "To assert fail result failStatusMatcher should be initialized");
        Assert.notNull(successViewName, "To assert fail result failViewName should be initialized");
        actions.andExpect(failStatusMatcher).andExpect(view().name(failViewName))
                .andExpect(model().attributeHasFieldErrors(name, fieldNames));
        return entityIdentifier;
    }
}
