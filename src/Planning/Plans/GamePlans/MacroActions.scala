package Planning.Plans.GamePlans

import Lifecycle.With

trait MacroActions {
  def status(text: String): Unit = With.blackboard.status.set(With.blackboard.status() :+ text)
  def attack(): Unit = With.blackboard.wantToAttack.set(true)
  def allIn(): Unit = { With.blackboard.allIn.set(true); attack() }
  def gasWorkerFloor(value: Int): Unit = With.blackboard.gasWorkerFloor.set(value)
  def gasWorkerCeiling(value: Int): Unit = With.blackboard.gasWorkerCeiling.set(value)
  def gasLimitFloor(value: Int): Unit = With.blackboard.gasLimitFloor.set(value)
  def gasLimitCeiling(value: Int): Unit = With.blackboard.gasLimitCeiling.set(value)
}
