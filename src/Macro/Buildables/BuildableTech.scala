package Macro.Buildables

import ProxyBwapi.Techs.Tech

case class BuildableTech(tech: Tech) extends Buildable {
  override def techOption : Option[Tech]  = Some(tech)
  override def toString   : String        = tech.toString
  override def frames     : Int           = tech.researchFrames
}
