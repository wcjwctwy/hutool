package cn.hutool.json.xml;

import cn.hutool.core.text.StrUtil;
import cn.hutool.json.InternalJSONUtil;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.XML;
import cn.hutool.json.XMLTokener;

/**
 * XML解析器，将XML解析为JSON对象
 *
 * @author JSON.org, looly
 * @since 5.7.11
 */
public class JSONXMLParser {

	/**
	 * 转换XML为JSONObject
	 * 转换过程中一些信息可能会丢失，JSON中无法区分节点和属性，相同的节点将被处理为JSONArray。
	 *
	 * @param jo          JSONObject
	 * @param xmlStr      XML字符串
	 * @param keepStrings 如果为{@code true}，则值保持String类型，不转换为数字或boolean
	 * @throws JSONException 解析异常
	 */
	public static void parseJSONObject(final JSONObject jo, final String xmlStr, final boolean keepStrings) throws JSONException {
		final XMLTokener x = new XMLTokener(xmlStr, jo.getConfig());
		while (x.more() && x.skipPast("<")) {
			parse(x, jo, null, keepStrings);
		}
	}

	/**
	 * Scan the content following the named tag, attaching it to the context.
	 *
	 * @param x       The XMLTokener containing the source string.
	 * @param context The JSONObject that will include the new material.
	 * @param name    The tag name.
	 * @return true if the close tag is processed.
	 * @throws JSONException JSON异常
	 */
	private static boolean parse(final XMLTokener x, final JSONObject context, final String name, final boolean keepStrings) throws JSONException {
		final char c;
		int i;
		final JSONObject jsonobject;
		String string;
		final String tagName;
		Object token;

		token = x.nextToken();

		if (token == XML.BANG) {
			c = x.next();
			if (c == '-') {
				if (x.next() == '-') {
					x.skipPast("-->");
					return false;
				}
				x.back();
			} else if (c == '[') {
				token = x.nextToken();
				if ("CDATA".equals(token)) {
					if (x.next() == '[') {
						string = x.nextCDATA();
						if (string.length() > 0) {
							context.append("content", string);
						}
						return false;
					}
				}
				throw x.syntaxError("Expected 'CDATA['");
			}
			i = 1;
			do {
				token = x.nextMeta();
				if (token == null) {
					throw x.syntaxError("Missing '>' after '<!'.");
				} else if (token == XML.LT) {
					i += 1;
				} else if (token == XML.GT) {
					i -= 1;
				}
			} while (i > 0);
			return false;
		} else if (token == XML.QUEST) {

			// <?
			x.skipPast("?>");
			return false;
		} else if (token == XML.SLASH) {

			// Close tag </

			token = x.nextToken();
			if (name == null) {
				throw x.syntaxError("Mismatched close tag " + token);
			}
			if (!token.equals(name)) {
				throw x.syntaxError("Mismatched " + name + " and " + token);
			}
			if (x.nextToken() != XML.GT) {
				throw x.syntaxError("Misshaped close tag");
			}
			return true;

		} else if (token instanceof Character) {
			throw x.syntaxError("Misshaped tag");

			// Open tag <

		} else {
			tagName = (String) token;
			token = null;
			jsonobject = new JSONObject();
			for (; ; ) {
				if (token == null) {
					token = x.nextToken();
				}

				// attribute = value
				if (token instanceof String) {
					string = (String) token;
					token = x.nextToken();
					if (token == XML.EQ) {
						token = x.nextToken();
						if (!(token instanceof String)) {
							throw x.syntaxError("Missing value");
						}
						jsonobject.append(string, keepStrings ? token : InternalJSONUtil.stringToValue((String) token));
						token = null;
					} else {
						jsonobject.append(string, "");
					}

				} else if (token == XML.SLASH) {
					// Empty tag <.../>
					if (x.nextToken() != XML.GT) {
						throw x.syntaxError("Misshaped tag");
					}
					if (jsonobject.size() > 0) {
						context.append(tagName, jsonobject);
					} else {
						context.append(tagName, StrUtil.EMPTY);
					}
					return false;

				} else if (token == XML.GT) {
					// Content, between <...> and </...>
					for (; ; ) {
						token = x.nextContent();
						if (token == null) {
							if (tagName != null) {
								throw x.syntaxError("Unclosed tag " + tagName);
							}
							return false;
						} else if (token instanceof String) {
							string = (String) token;
							if (string.length() > 0) {
								jsonobject.append("content", keepStrings ? token : InternalJSONUtil.stringToValue(string));
							}

						} else if (token == XML.LT) {
							// Nested element
							if (parse(x, jsonobject, tagName, keepStrings)) {
								if (jsonobject.size() == 0) {
									context.append(tagName, "");
								} else if (jsonobject.size() == 1 && jsonobject.get("content") != null) {
									context.append(tagName, jsonobject.get("content"));
								} else {
									context.append(tagName, jsonobject);
								}
								return false;
							}
						}
					}
				} else {
					throw x.syntaxError("Misshaped tag");
				}
			}
		}
	}
}
