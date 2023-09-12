package Planning.Plans.GamePlans.Protoss.PvZ

import Debugging.SimpleString
import Information.Fingerprinting.ZergStrategies.ZergTimings._
import Lifecycle.With

abstract class PvZ1BaseReactiveUtilities extends PvZ1BaseBuildOrders {
  protected trait   Economy extends SimpleString
  protected object  Aggro   extends Economy
  protected object  Greedy  extends Economy
  protected object  Neutral extends Economy

  protected trait   Opening     extends SimpleString
  protected object  Gates910    extends Opening
  protected object  Gates1012   extends Opening
  protected object  CoreZ       extends Opening
  protected object  ZCoreZ      extends Opening
  protected object  ZZCoreZ     extends Opening
  protected object  GateNexus   extends Opening
  protected object  NexusFirst  extends Opening

  protected trait   Composition extends SimpleString
  protected object  Goon        extends Composition
  protected object  Speedlot    extends Composition
  protected object  Reaver      extends Composition
  protected object  Stargate    extends Composition

  protected val timing4p    = 0
  protected val timing9p    = 1
  protected val timingOp    = 2
  protected val timing12p   = 3
  protected val timing10h   = 4
  protected val timing12h   = 5
  protected val timing12hh  = 6

  protected def pool: Option[Int] = {
    if (With.fingerprints.twelveHatchHatch()) return Some(timing12hh)
    if (With.fingerprints.twelveHatch())      return Some(timing12h)
    if (With.fingerprints.hatchFirst())       return Some(timing10h)
    if (With.fingerprints.twelvePool())       return Some(timing12p)
    if (With.fingerprints.overpool())         return Some(timingOp)
    if (With.fingerprints.ninePool())         return Some(timing9p)
    if (With.fingerprints.fourPool())         return Some(timing4p)
    None
  }
  protected def poolMinHistory: Option[Int] = {
    if (With.fingerprints.fourPool        .recently) return Some(timing4p)
    if (With.fingerprints.ninePool        .recently) return Some(timing9p)
    if (With.fingerprints.overpool        .recently) return Some(timingOp)
    if (With.fingerprints.twelvePool      .recently) return Some(timing12p)
    if (With.fingerprints.hatchFirst      .recently) return Some(timing10h)
    if (With.fingerprints.twelveHatch     .recently) return Some(timing12h)
    if (With.fingerprints.twelveHatchHatch.recently) return Some(timing12hh)
    None
  }
  protected def poolMaxHistory: Option[Int] = {
    if (With.fingerprints.twelveHatchHatch.recently) return Some(timing12hh)
    if (With.fingerprints.twelveHatch     .recently) return Some(timing12h)
    if (With.fingerprints.hatchFirst      .recently) return Some(timing10h)
    if (With.fingerprints.twelvePool      .recently) return Some(timing12p)
    if (With.fingerprints.overpool        .recently) return Some(timingOp)
    if (With.fingerprints.ninePool        .recently) return Some(timing9p)
    if (With.fingerprints.fourPool        .recently) return Some(timing4p)
    None
  }
  protected def poolMin: Int = pool.getOrElse(
    if      (With.frame < Latest_FourPool_ZerglingArrivesBy())    timing4p
    else if (With.frame < Latest_NinePool_ZerglingArrivesBy())    timing9p
    else if (With.frame < Latest_Overpool_ZerglingArrivesBy())    timingOp
    else if (With.frame < Latest_TwelvePool_ZerglingArrivesBy())  timing12p
    else if (With.frame < Latest_TenHatch_ZerglingArrivesBy())    timing10h
    else                                                          timing12h
  )
  protected def poolMax: Int = pool.getOrElse(timing12hh)
  protected def poolMinRecently: Int = pool.orElse(poolMinHistory).getOrElse(timing9p)
  protected def poolMaxRecently: Int = pool.orElse(poolMaxHistory).getOrElse(timingOp)

  val allOpenings: Seq[Opening] = Seq(Gates910, Gates1012, CoreZ, ZCoreZ, ZZCoreZ, GateNexus, NexusFirst)
}
