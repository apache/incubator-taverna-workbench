/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*

package org.apache.taverna.lang.ui;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;


/**
 * 
 * The following class is copied from http://forums.sun.com/thread.jspa?threadID=622683
 *
 */
public class NoWrapEditorKit extends StyledEditorKit
{
	public ViewFactory getViewFactory()
	{
			return new StyledViewFactory();
	} 
 
	static class StyledViewFactory implements ViewFactory
	{
		public View create(Element elem)
		{
			String kind = elem.getName();
 
			if (kind != null)
			{
				if (kind.equals(AbstractDocument.ContentElementName))
				{
					return new LabelView(elem);
				}
				else if (kind.equals(AbstractDocument.ParagraphElementName))
				{
					return new ParagraphView(elem);
				}
				else if (kind.equals(AbstractDocument.SectionElementName))
				{
					return new NoWrapBoxView(elem, View.Y_AXIS);
				}
				else if (kind.equals(StyleConstants.ComponentElementName))
				{
					return new ComponentView(elem);
				}
				else if (kind.equals(StyleConstants.IconElementName))
				{
					return new IconView(elem);
				}
			}
 
	 		return new LabelView(elem);
		}
	}

	static class NoWrapBoxView extends BoxView {
        public NoWrapBoxView(Element elem, int axis) {
            super(elem, axis);
        }
 
        public void layout(int width, int height) {
            super.layout(32768, height);
        }
        public float getMinimumSpan(int axis) {
            return super.getPreferredSpan(axis);
        }
    }
}
