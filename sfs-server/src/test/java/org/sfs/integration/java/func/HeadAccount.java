/*
 * Copyright 2016 The Simple File Server Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sfs.integration.java.func;

import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.logging.Logger;
import org.sfs.rx.ObservableFuture;
import org.sfs.rx.RxHelper;
import rx.Observable;
import rx.functions.Func1;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static io.vertx.core.logging.LoggerFactory.getLogger;
import static org.sfs.integration.java.help.AuthorizationFactory.Producer;

public class HeadAccount implements Func1<Void, Observable<HttpClientResponse>> {

    private static final Logger LOGGER = getLogger(HeadAccount.class);
    private final HttpClient httpClient;
    private final String accountName;
    private final Producer auth;

    public HeadAccount(HttpClient httpClient, String accountName, Producer auth) {
        this.httpClient = httpClient;
        this.accountName = accountName;
        this.auth = auth;
    }

    @Override
    public Observable<HttpClientResponse> call(Void aVoid) {
        return auth.toHttpAuthorization()
                .flatMap(new Func1<String, Observable<HttpClientResponse>>() {
                    @Override
                    public Observable<HttpClientResponse> call(String s) {
                        ObservableFuture<HttpClientResponse> handler = RxHelper.observableFuture();
                        httpClient.head("/openstackswift001/" + accountName, handler::complete)
                                .exceptionHandler(handler::fail)
                                .setTimeout(10000)
                                .putHeader(AUTHORIZATION, s)
                                .end();
                        return handler
                                .single();
                    }
                });
    }
}
