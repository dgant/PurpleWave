package ProxyBwapi.Techs

import Lifecycle.With
import bwapi.TechType

object Techs {
  def all: Vector[Tech] = With.proxy.techs
  def get(tech: TechType): Tech = With.proxy.techsById(tech.id)
  def None: Tech = get(TechType.None)
  def Unknown: Tech = get(TechType.Unknown)
}
