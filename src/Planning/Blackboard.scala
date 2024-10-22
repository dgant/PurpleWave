package Planning

import Information.Geography.Types.Base
import Lifecycle.With
import ProxyBwapi.Buildable
import ProxyBwapi.UnitClasses.UnitClass
import Utilities.Property
import Utilities.Time.Seconds

class Blackboard {
  
  protected var resets: Vector[() => Unit] = Vector.empty
  protected def add[T](property: Property[T]): Property[T] = {
    resets = resets :+ (() => property.reset())
    property
  }
  
  def reset(): Unit = {
    resets.foreach(reset => reset())
  }

  val status                  : Property[Seq[String]]       = add(new Property(Seq.empty))
  val aggressionRatio         : Property[Double]            = add(new Property(1.0))
  val wantToAttack            : Property[Boolean]           = add(new Property(false))
  val wantToHarass            : Property[Boolean]           = add(new Property(false))
  val scoutExpansions         : Property[Boolean]           = add(new Property(true))
  val stealGas                : Property[Boolean]           = add(new Property(false))
  val maximumScouts           : Property[Int]               = add(new Property(0))
  val gasWorkerFloor          : Property[Int]               = add(new Property(0))      // Require at least this many gas workers
  val gasWorkerCeiling        : Property[Int]               = add(new Property(200))    // Require no more than this many gas workers (unless saturated on minerals)
  val gasLimitFloor           : Property[Int]               = add(new Property(0))      // Max gas mining until at least this much gas
  val gasLimitCeiling         : Property[Int]               = add(new Property(500))    // Stop gas mining (if minerals are available) after this much gas
  val maxBuilderTravelFrames  : Property[Int]               = add(new Property(Seconds(50)()))
  val gasWorkerRatio          : Property[Double]            = add(new Property(if (With.self.isProtoss) 3.0 / 10.0 else 3.0 / 8.0))
  val crossScout              : Property[Boolean]           = add(new Property(false))
  val yoloing                 : Property[Boolean]           = add(new Property(false))
  val allowIslandBases        : Property[Boolean]           = add(new Property(false))
  val monitorBases            : Property[Boolean]           = add(new Property(false))
  val makeDarkArchons         : Property[Boolean]           = add(new Property(false))
  val floatableBuildings      : Property[Vector[UnitClass]] = add(new Property(Vector.empty))
  val toCancel                : Property[Vector[Buildable]] = add(new Property(Vector.empty))
  val basesToHold             : Property[Vector[Base]]      = add(new Property(Vector.empty))
}
