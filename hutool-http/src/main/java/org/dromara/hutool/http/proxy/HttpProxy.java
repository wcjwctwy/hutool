/*
 * Copyright (c) 2023 looly(loolly@aliyun.com)
 * Hutool is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.dromara.hutool.http.proxy;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * HTTP代理，提供代理地址和代理端口的持有。
 *
 * @author looly
 * @since 6.0.0
 */
public class HttpProxy extends Proxy {

	private final String host;
	private final int port;

	/**
	 * 构造
	 *
	 * @param host 域名或IP
	 * @param port 端口
	 */
	public HttpProxy(final String host, final int port) {
		super(Type.HTTP, new InetSocketAddress(host, port));
		this.host = host;
		this.port = port;
	}

	/**
	 * 获取域名或IP
	 *
	 * @return 域名或IP
	 */
	public String getHost() {
		return host;
	}

	/**
	 * 获取端口
	 *
	 * @return 端口
	 */
	public int getPort() {
		return port;
	}

}