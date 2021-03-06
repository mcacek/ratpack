/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ratpack.http.client.internal;

import io.netty.handler.ssl.SslContext;
import ratpack.func.Action;
import ratpack.func.Factory;
import ratpack.func.Function;
import ratpack.http.HttpMethod;
import ratpack.http.MutableHeaders;
import ratpack.http.client.ReceivedResponse;
import ratpack.http.client.RequestSpec;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.time.Duration;

public class DelegatingRequestSpec implements RequestSpec {

  private final RequestSpec delegate;

  public DelegatingRequestSpec(RequestSpec delegate) {
    this.delegate = delegate;
  }

  protected RequestSpec getDelegate() {
    return delegate;
  }

  @Override
  public RequestSpec redirects(int maxRedirects) {
    delegate.redirects(maxRedirects);
    return this;
  }

  @Override
  public RequestSpec onRedirect(Function<? super ReceivedResponse, Action<? super RequestSpec>> function) {
    delegate.onRedirect(function);
    return this;
  }

  @Override
  @SuppressWarnings("deprecation")
  public RequestSpec sslContext(SSLContext sslContext) {
    delegate.sslContext(sslContext);
    return this;
  }

  @Override
  @SuppressWarnings("deprecation")
  public RequestSpec sslContext(Factory<SSLContext> factory) throws Exception {
    delegate.sslContext(factory);
    return this;
  }

  @Override
  public RequestSpec sslContext(SslContext sslContext) {
    delegate.sslContext(sslContext);
    return this;
  }

  @Override
  public MutableHeaders getHeaders() {
    return delegate.getHeaders();
  }

  @Override
  public RequestSpec headers(Action<? super MutableHeaders> action) throws Exception {
    delegate.headers(action);
    return this;
  }

  @Override
  public RequestSpec method(HttpMethod method) {
    delegate.method(method);
    return this;
  }

  @Override
  public RequestSpec decompressResponse(boolean shouldDecompress) {
    delegate.decompressResponse(shouldDecompress);
    return this;
  }

  @Override
  public URI getUri() {
    return delegate.getUri();
  }

  @Override
  public RequestSpec connectTimeout(Duration duration) {
    delegate.connectTimeout(duration);
    return this;
  }

  @Override
  public RequestSpec readTimeout(Duration duration) {
    delegate.readTimeout(duration);
    return this;
  }

  @Override
  public Body getBody() {
    return delegate.getBody();
  }

  @Override
  public RequestSpec body(Action<? super Body> action) throws Exception {
    delegate.body(action);
    return this;
  }

  @Override
  public RequestSpec basicAuth(String username, String password) {
    delegate.basicAuth(username, password);
    return this;
  }

  @Override
  public RequestSpec maxContentLength(int numBytes) {
    return delegate.maxContentLength(numBytes);
  }

  @Override
  public RequestSpec responseMaxChunkSize(int numBytes) {
    delegate.responseMaxChunkSize(numBytes);
    return this;
  }

}
