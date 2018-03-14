/*
 * Copyright © 2018 Apple Inc. and the ServiceTalk project authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.servicetalk.concurrent.api;

import io.servicetalk.concurrent.Cancellable;

import static java.util.Objects.requireNonNull;

final class DoCancellableSingle<T> extends Single<T> {
    private final Single<T> original;
    private final Cancellable cancellable;
    private final boolean before;

    DoCancellableSingle(Single<T> original, Cancellable cancellable, boolean before) {
        this.original = requireNonNull(original);
        this.cancellable = requireNonNull(cancellable);
        this.before = before;
    }

    @Override
    protected void handleSubscribe(Subscriber<? super T> subscriber) {
        original.subscribe(new DoBeforeCancellableSubscriber<>(subscriber, this));
    }

    private static final class DoBeforeCancellableSubscriber<T> implements Subscriber<T> {
        private final Subscriber<? super T> original;
        private final DoCancellableSingle parent;

        DoBeforeCancellableSubscriber(Subscriber<? super T> original, DoCancellableSingle parent) {
            this.original = original;
            this.parent = parent;
        }

        @Override
        public void onSubscribe(Cancellable originalCancellable) {
            original.onSubscribe(parent.before ? new DoBeforeCancellable(parent.cancellable, originalCancellable) : new DoBeforeCancellable(originalCancellable, parent.cancellable));
        }

        @Override
        public void onSuccess(T value) {
            original.onSuccess(value);
        }

        @Override
        public void onError(Throwable t) {
            original.onError(t);
        }
    }
}
