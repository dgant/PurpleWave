package ProxyBwapi.Techs

import Lifecycle.With
import bwapi.TechType

object Techs {
  def get(tech: TechType): Tech = With.proxy.techsByType.getOrElse(tech, With.proxy.techsByName(tech.toString))
  def all: Iterable[Tech] = With.proxy.techsByType.values
  def None: Tech = get(TechType.None)
  def Unknown: Tech = get(TechType.Unknown)
}
