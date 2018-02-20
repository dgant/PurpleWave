package Planning.Composition.ResourceLocks

import Planning.Plan

trait ResourceLock {
  def satisfied: Boolean
  def acquire(plan: Plan)
  def release()
}
