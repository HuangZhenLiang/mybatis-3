/**
 *    Copyright 2009-2017 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.builder.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.*;

/**
 * @author huangzhenliang
 */
public class XMLMergeUtil {

	static class DuplicateChildElementObject {
		private boolean needDuplicate = true;
		private Element element = null;

		public DuplicateChildElementObject() {
			super();
		}

		public boolean isNeedDuplicate() {
			return needDuplicate;
		}

		public void setNeedDuplicate(boolean needDuplicate) {
			this.needDuplicate = needDuplicate;
		}

		public Element getElement() {
			return element;
		}

		public void setElement(Element element) {
			this.element = element;
		}
	}

	public static void merge(Document docMain, String path) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();  
        DocumentBuilder db = null;  
        db = dbf.newDocumentBuilder();
        db.setEntityResolver(new XMLMapperEntityResolver());
        Document docVice = db.parse(XMLMergeUtil.class.getResourceAsStream(path));
		merge(docMain, docVice);
	}

	public static void merge(Document docMain, Document docVice) throws Exception {
		Element rootMain = docMain.getDocumentElement();
		Element rootVice = docVice.getDocumentElement();
		NodeList messageItems = rootVice.getChildNodes();
		int item_number = messageItems.getLength();
		for (int i = 1; i < item_number; i = i + 2) {
			Node node = messageItems.item(i);
			if (node instanceof Element) {
				Element messageItem = (Element) node;
				dupliate(docMain, rootMain, messageItem);
			}
		}
	}

	private static boolean dupliate(Document docMain, Element father, Element son) throws Exception {
		boolean isdone = false;
		Element parentElement = null;

		DuplicateChildElementObject childElementObject = isChildElement(father, son);
		if (!childElementObject.isNeedDuplicate()) {
			isdone = true;
			parentElement = childElementObject.getElement();
		} else if (childElementObject.getElement() != null) {
			parentElement = childElementObject.getElement();
		} else {
			parentElement = father;
		}

		String son_name = son.getNodeName();
		Element subITEM = null;
		if (!isdone) {
			subITEM = docMain.createElement(son_name);
			if (son.hasAttributes()) {
				NamedNodeMap attributes = son.getAttributes();
				for (int i = 0; i < attributes.getLength(); i++) {
					String attribute_name = attributes.item(i).getNodeName();
					String attribute_value = attributes.item(i).getNodeValue();
					subITEM.setAttribute(attribute_name, attribute_value);
				}
			}
			parentElement.appendChild(subITEM);
		} else {
			subITEM = parentElement;
		}

		NodeList sub_messageItems = son.getChildNodes();
		int sub_item_number = sub_messageItems.getLength();
		if (sub_item_number < 2) {
			isdone = true;
		} else {
			for (int j = 1; j < sub_item_number; j = j + 2) {
				Element sub_messageItem = (Element) sub_messageItems.item(j);
				isdone = dupliate(docMain, subITEM, sub_messageItem);
			}
		}

		return isdone;
	}

	private static DuplicateChildElementObject isChildElement(Element father, Element son) {

		DuplicateChildElementObject childElementObject = new DuplicateChildElementObject();

		NodeList messageItems = father.getChildNodes();
		int item_number = messageItems.getLength();
		for (int i = 1; i < item_number; i = i + 2) {
			Element messageItem = (Element) messageItems.item(i);
			if (!messageItem.getNodeName().equals(son.getNodeName())) {
				continue;
			}
			if (messageItem.isEqualNode(son)) {
				childElementObject.setNeedDuplicate(false);
				childElementObject.setElement(messageItem);
				return childElementObject;
			}
		}
		for (int i = 1; i < item_number; i = i + 2) {
			Element messageItem = (Element) messageItems.item(i);
			if (!messageItem.getNodeName().equals(son.getNodeName())) {
				continue;
			}
			if (isEqualNode(messageItem, son)) {
				if (hasEqualAttributes(messageItem, son)) {
					childElementObject.setNeedDuplicate(false);
					childElementObject.setElement(messageItem);
					return childElementObject;
				} else {
					childElementObject.setNeedDuplicate(true);
					childElementObject.setElement(father);
					return childElementObject;
				}
			}
		}

		childElementObject.setNeedDuplicate(true);
		childElementObject.setElement(father);
		return childElementObject;
	}

	private static boolean isEqualNode(Node arg0, Node arg) {
		if (arg == arg0) {
			return true;
		}
		if (arg.getNodeType() != arg0.getNodeType()) {
			return false;
		}

		if (arg0.getNodeName() == null) {
			if (arg.getNodeName() != null) {
				return false;
			}
		} else if (!arg0.getNodeName().equals(arg.getNodeName())) {
			return false;
		}

		if (arg0.getLocalName() == null) {
			if (arg.getLocalName() != null) {
				return false;
			}
		} else if (!arg0.getLocalName().equals(arg.getLocalName())) {
			return false;
		}

		if (arg0.getNamespaceURI() == null) {
			if (arg.getNamespaceURI() != null) {
				return false;
			}
		} else if (!arg0.getNamespaceURI().equals(arg.getNamespaceURI())) {
			return false;
		}

		if (arg0.getPrefix() == null) {
			if (arg.getPrefix() != null) {
				return false;
			}
		} else if (!arg0.getPrefix().equals(arg.getPrefix())) {
			return false;
		}

		if (arg0.getNodeValue() == null) {
			if (arg.getNodeValue() != null) {
				return false;
			}
		} else if (!arg0.getNodeValue().equals(arg.getNodeValue())) {
			return false;
		}
		return true;
	}

	private static boolean hasEqualAttributes(Node arg0, Node arg) {

		NamedNodeMap map1 = arg0.getAttributes();
		NamedNodeMap map2 = arg.getAttributes();
		int len = map1.getLength();
		if (len != map2.getLength()) {
			return false;
		}

		for (int i = 0; i < len; i++) {
			Node n1 = map1.item(i);
			if (n1.getNodeName() != null) {
				Node n2 = map2.getNamedItem(n1.getNodeName());
				if (n2 == null) {
					return false;
				} else if (!n1.getNodeValue().equals(n2.getNodeValue())) {
					return false;
				}
			}
		}
		return true;
	}
}
