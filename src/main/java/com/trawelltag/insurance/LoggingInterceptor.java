package com.trawelltag.insurance;


import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

public class LoggingInterceptor implements Interceptor {
	
	
	@Override
	public Response intercept(Chain chain) throws IOException {
		Request request = chain.request();
		Buffer requestBuffer = new Buffer();
		request.body().writeTo(requestBuffer);
		System.out.println("\n request: "+requestBuffer.readUtf8());

		Response response = chain.proceed(request);

		MediaType contentType = response.body().contentType();
		String content = response.body().string();
		System.out.println("response: "+content);

		ResponseBody wrappedBody = ResponseBody.create(contentType, content);
		return response.newBuilder().body(wrappedBody).build();
	}
}