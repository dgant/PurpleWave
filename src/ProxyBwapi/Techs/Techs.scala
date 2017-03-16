package ProxyBwapi.Techs

import bwapi.TechType

object Techs {
  val get:Map[TechType, Tech] = TechTypes.all.map(techType => (techType, new Tech(techType))).toMap
  val all = get.values.toList
}
