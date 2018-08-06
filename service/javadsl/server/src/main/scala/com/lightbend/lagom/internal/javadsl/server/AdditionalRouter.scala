/*
 * Copyright (C) 2016-2018 Lightbend Inc. <https://www.lightbend.com>
 */

package com.lightbend.lagom.internal.javadsl.server

import java.util

import com.lightbend.lagom.javadsl.server.AdditionalRouters
import javax.inject.Provider
import play.api.inject.Injector
import play.routing.Router
import scala.collection.JavaConverters._
import scala.collection.immutable

trait AdditionalRouter {

  def prefix: Option[String]
  def withPrefix(path: String): AdditionalRouter
}

final case class ClassBased[R <: Router](classType: Class[R], prefix: Option[String]) extends AdditionalRouter {

  def this(classType: Class[R]) = this(classType, None)

  def withPrefix(path: String) = copy(prefix = Option(path))
}

final case class InstanceBased(router: Router, prefix: Option[String]) extends AdditionalRouter {

  def this(classType: Router) = this(classType, None)

  def withPrefix(path: String) = copy(prefix = Option(path))
}

object AdditionalRouter {

  def wireRouters(injector: Injector, additionalRouters: util.List[AdditionalRouter]): util.List[Router] = {

    // modifies the Router in case a prefix is defined
    def applyPrefix(router: Router, prefix: Option[String]): Router =
      prefix.map(router.withPrefix).getOrElse(router)

    additionalRouters.asScala.foldLeft(immutable.Seq.empty[Router]) {
      case (routers, ar: InstanceBased) =>
        routers :+ applyPrefix(ar.router, ar.prefix)

      case (routers, ar: ClassBased[_]) =>
        val ins = injector.instanceOf(ar.classType)
        routers :+ applyPrefix(ins, ar.prefix)
    }.asJava
  }
}

