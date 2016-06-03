/*
 * Copyright (c) 2016 Algolia
 * http://www.algolia.com/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package algolia.http

import com.netaporter.uri.dsl._
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.resolver.dns.{DnsNameResolver, DnsNameResolverBuilder}
import org.asynchttpclient.{AsyncCompletionHandler, Request, RequestBuilder, Response}

object Req {

  val dnsNameResolver: DnsNameResolver =
    new DnsNameResolverBuilder(new NioEventLoopGroup(1).next())
      .channelType(classOf[NioDatagramChannel])
      .queryTimeoutMillis(100)
      .build

  def apply(host: String, headers: Map[String, String], payload: HttpPayload): Req = {
    val uri = payload.path.foldLeft(host)((url, p) => url / p)

    var builder = new RequestBuilder().setMethod(payload.verb.toString).setUrl(uri)

    headers.foreach { case (k, v) => builder = builder.addHeader(k, v) }

    payload.queryParameters.foreach(
      _.foreach { case (k, v) => builder = builder.addQueryParam(k, v) }
    )

    payload.body.foreach(b => builder = builder.setBody(b))

    Req(builder)
  }
}

case class Req(builder: RequestBuilder) {

  def >[T](f: Response => T) =
    (toRequest, new AsyncCompletionHandler[T] {
      override def onCompleted(response: Response) = f(response)
    })

  def toRequest: Request =
    builder.setNameResolver(Req.dnsNameResolver).build

}
