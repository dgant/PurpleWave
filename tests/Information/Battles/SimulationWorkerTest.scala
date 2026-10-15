package Information.Battles.Prediction.Simulation

import Debugging.Logger
import Information.Battles.Types.Battle
import Lifecycle.Configure.Configuration
import Lifecycle.With
import Planning.Blackboard
import ProxyBwapi.ProxyBWAPI
import ProxyBwapi.UnitClasses.{UnitClass, UnitTypes}
import Utilities.Property
import java.util.concurrent.{CountDownLatch, TimeUnit}

object SimulationWorkerTest extends App {
  // Synthetic game state, with the production Simulation, step, completion and worker lifecycle.
  val unsafeField = classOf[sun.misc.Unsafe].getDeclaredField("theUnsafe")
  unsafeField.setAccessible(true)
  val unsafe = unsafeField.get(null).asInstanceOf[sun.misc.Unsafe]
  def allocate[T](clazz: Class[T]): T = unsafe.allocateInstance(clazz).asInstanceOf[T]
  def field(obj: AnyRef, name: String): java.lang.reflect.Field = {
    val f = obj.getClass.getDeclaredFields.find(f => f.getName == name || f.getName.endsWith("$$" + name)).get
    f.setAccessible(true)
    f
  }
  def set(obj: AnyRef, name: String, value: AnyRef): Unit = field(obj, name).set(obj, value)
  def worker(sim: Simulation): Thread = field(sim, "thread").get(sim).asInstanceOf[Thread]
  def await(message: String)(condition: => Boolean): Unit = {
    val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
    while (!condition && System.nanoTime() < deadline) Thread.`yield`()
    assert(condition, message)
  }
  def stop(sim: Simulation): Unit = {
    sim.onEnd()
    val t = worker(sim)
    if (t != null) { t.join(5000); assert(!t.isAlive, "shutdown left a live worker") }
  }
  def battle(): Battle = new Battle(isGlobal = false) {
    override lazy val simulated: Boolean = true
    override lazy val logSimulation: Boolean = false
  }
  def simulation(): Simulation = {
    val sim = new Simulation
    sim.shouldReset = false
    sim.battle = battle()
    sim
  }
  With.configuration = new Configuration
  With.configuration.debugging = false
  With.configuration.logstd = false
  With.proxy = allocate(classOf[ProxyBWAPI])
  set(With.proxy, "unitClasses", UnitTypes.all.map(UnitClass))
  val blackboard = allocate(classOf[Blackboard])
  set(blackboard, "aggressionRatio", new Property(1.0))
  With.blackboard = blackboard
  With.logger = new Logger

  val sim = simulation()
  try {
    // Immediate resubmission stresses completion -> wait races; gaps exercise already-waiting wakes.
    var i = 0
    while (i < 20000) {
      val next = battle()
      next.simulationFrames = i
      sim.battle = next
      sim.runAsynchronously()
      await("missed work notification")(!sim.simulatingAsynchronously)
      assert(next.simulationComplete && next.simulationFrames == i + 1, "wrong battle or duplicated step")
      assert(next.simulationCheckpoints.size == 1 && next.simulationCheckpoints.head.framesIn == i + 1,
        "completion did not publish the final checkpoint")
      if (i % 100 == 0) Thread.sleep(1)
      i += 1
    }
    await("idle worker did not wait")(worker(sim).getState == Thread.State.WAITING)
    val wakeup = field(sim, "wakeup").get(sim)
    wakeup.synchronized { wakeup.notifyAll() }
    worker(sim).interrupt()
    await("interrupted worker did not return to waiting")(worker(sim).getState == Thread.State.WAITING)
    assert(!sim.simulatingAsynchronously && sim.battle.simulationFrames == 20000, "spurious wake ran work")
    sim.battle = battle()
    sim.runAsynchronously()
    await("work after interrupt was lost")(!sim.simulatingAsynchronously)
    assert(sim.battle.simulationComplete)
  } finally stop(sim)
  println("PASS 20,000 handoffs, result publication, idle wait, spurious notification and interrupt recovery")

  val failed = simulation()
  val reported = new CountDownLatch(1)
  val retry = new CountDownLatch(1)
  With.logger = new Logger {
    override def onException(exception: Exception): Unit = {
      reported.countDown()
      assert(retry.await(5, TimeUnit.SECONDS), "test did not release error handler")
    }
  }
  With.blackboard = null // Force checkpoint to fail inside the real step method.
  try {
    failed.runAsynchronously()
    assert(reported.await(5, TimeUnit.SECONDS), "no simulation exception")
    assert(failed.simulatingAsynchronously, "exception released battle ownership before retry")
    With.blackboard = blackboard
    retry.countDown()
    await("worker did not recover after exception")(!failed.simulatingAsynchronously)
    assert(failed.battle.simulationComplete && failed.battle.simulationCheckpoints.size == 1)
  } finally { retry.countDown(); stop(failed); With.blackboard = blackboard }
  println("PASS exception keeps battle ownership and retries to completion")

  val active = simulation()
  val entered = new CountDownLatch(1)
  val release = new CountDownLatch(1)
  active.battle = new Battle(isGlobal = false) {
    override lazy val simulated: Boolean = true
    override lazy val logSimulation: Boolean = {
      entered.countDown()
      assert(release.await(5, TimeUnit.SECONDS), "test did not release active step")
      false
    }
  }
  try {
    active.runAsynchronously()
    assert(entered.await(5, TimeUnit.SECONDS), "worker did not start")
    active.onEnd() // Must return while the step is blocked; the work itself must not hold wakeup.
    release.countDown()
  } finally { release.countDown(); stop(active) }
  println("PASS shutdown during a step")

  val retrying = simulation()
  val failures = new java.util.concurrent.atomic.AtomicInteger
  With.blackboard = null
  With.logger = new Logger {
    override def onException(exception: Exception): Unit = { failures.incrementAndGet() }
  }
  try {
    retrying.runAsynchronously()
    await("worker did not retry a persistent exception")(failures.get() >= 2)
    assert(retrying.simulatingAsynchronously, "retry released ownership")
    await("retry did not back off")(worker(retrying).getState == Thread.State.TIMED_WAITING)
    val previous = failures.get()
    worker(retrying).interrupt()
    await("interrupt killed retrying worker")(failures.get() > previous)
  } finally { stop(retrying); With.blackboard = blackboard; With.logger = new Logger }
  println("PASS interrupted retry and shutdown with a persistent exception")

  for (_ <- 0 until 200) {
    val startup = simulation()
    startup.runAsynchronously()
    stop(startup)
    startup.runAsynchronously()
    assert(!worker(startup).isAlive && !startup.simulatingAsynchronously, "submission restarted a terminated worker")
  }
  val neverStarted = simulation()
  stop(neverStarted)
  neverStarted.runAsynchronously()
  assert(worker(neverStarted) == null, "shutdown before startup created a worker")
  println("PASS startup/shutdown race and submissions after shutdown")
}
