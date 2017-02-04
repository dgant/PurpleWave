package Traits

import scala.collection.mutable

trait TraitCascade {
  val _cascadingTraits:mutable.HashMap[String, String] = mutable.HashMap.empty
}
