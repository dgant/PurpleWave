package ProxyBwapi.Techs

import Performance.Caching.CacheForever
import bwapi.TechType

object Techs {
  def get(tech: TechType):Tech = mapping.get(tech)
  def all:Iterable[Tech] = mapping.get.values
  def None:Tech = get(TechType.None)
  def Unknown:Tech = get(TechType.Unknown)
  
  private val mapping = new CacheForever[Map[TechType, Tech]](() => TechTypes.all.map(TechType => (TechType, new Tech(TechType))).toMap)
}
