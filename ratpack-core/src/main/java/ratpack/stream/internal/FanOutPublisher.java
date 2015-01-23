/*
 * Copyright 2014 the original author or authors.
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

package ratpack.stream.internal;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import ratpack.stream.TransformablePublisher;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class FanOutPublisher<T> implements TransformablePublisher<T> {

  private final Publisher<? extends Iterable<T>> upstream;

  public FanOutPublisher(Publisher<? extends Iterable<T>> upstream) {
    this.upstream = upstream;
  }

  // Note, we know that a buffer publisher is downstream

  @Override
  public void subscribe(final Subscriber<? super T> downstream) {
    upstream.subscribe(new Subscriber<Iterable<T>>() {

      private final AtomicBoolean done = new AtomicBoolean();

      @Override
      public void onSubscribe(Subscription subscription) {
        downstream.onSubscribe(new Subscription() {
          @Override
          public void request(long n) {
            subscription.request(n);
          }

          @Override
          public void cancel() {
            done.set(true);
            subscription.cancel();
          }
        });
      }

      @Override
      public void onNext(Iterable<T> iterable) {
        Iterator<T> iterator = iterable.iterator();
        while (iterator.hasNext() && !done.get()) {
          downstream.onNext(iterator.next());
        }
      }

      @Override
      public void onError(Throwable t) {
        downstream.onError(t);
      }

      @Override
      public void onComplete() {
        downstream.onComplete();
      }
    });
  }

}
