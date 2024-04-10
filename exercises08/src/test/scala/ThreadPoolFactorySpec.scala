package exercises08

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import java.io.Closeable
import java.lang.Thread.State
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.TimeUnit
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{DurationInt, FiniteDuration}

class ThreadPoolFactorySpec extends AnyFlatSpec {

  trait scenario {
    def inf: FiniteDuration    = FiniteDuration.apply(100, TimeUnit.DAYS)
    val atomicLong: AtomicLong = new AtomicLong(0)
    def task: Runnable = new Runnable {
      override def run(): Unit = atomicLong.incrementAndGet()
    }

    def taskDelayed: Runnable = new Runnable {

      override def run(): Unit = {
        Thread.sleep(25)
        atomicLong.incrementAndGet()
      }
    }
    def minThreads: Int
    def maxThreads: Int
    def ttl: FiniteDuration
    val threadsArray: ArrayBuffer[Thread] = new ArrayBuffer[Thread]()

    val factory: MyThreadFactory = new MyThreadFactory {
      override def startThread(task: () => Unit): Thread = {
        val t = new Thread(new Runnable {
          override def run(): Unit =
            try {
              task()
            } catch {
              case e: InterruptedException => ()
            }
        })
        threadsArray.addOne(t)
        t
      }
    }

    val tPool: ExecutionContext with Closeable = ThreadPool.makeThreadPoolEC(
      maxThreads = maxThreads,
      minThreads = minThreads,
      ttl = ttl
    )(factory)
  }

  trait scenarioFixed extends scenario {
    def threads: Int

    override def minThreads: Int = threads

    override def maxThreads: Int = threads

    override def ttl: FiniteDuration = inf
  }

  "ThreadPool" should "work with single thread" in new scenarioFixed {

    override def threads: Int = 1
    List.range(0, 100).foreach(i => tPool.execute(task))
    Thread.sleep(75)
    threadsArray.size shouldBe 1
    atomicLong.get() shouldBe 100
    tPool.close()
  }

  "ThreadPool" should "work with 7 thread" in new scenarioFixed {

    override def threads: Int = 7
    List.range(0, 100).foreach(i => tPool.execute(task))
    Thread.sleep(75)
    threadsArray.size shouldBe 7
    atomicLong.get() shouldBe 100
    tPool.close()
  }

  "ThreadPool" should "work concurrent with 10 thread" in new scenarioFixed {

    override def threads: Int = 10
    List.range(0, 100).foreach(i => tPool.execute(taskDelayed))
    Thread.sleep(3000)
    threadsArray.size shouldBe 10
    atomicLong.get() shouldBe 100
    tPool.close()
  }

  "ThreadPool" should "don't work concurrent with 1 thread" in new scenarioFixed {

    override def threads: Int = 1
    List.range(0, 100).foreach(i => tPool.execute(taskDelayed))
    Thread.sleep(600)
    threadsArray.size shouldBe 1
    assert(atomicLong.get() < 70)
    assert(atomicLong.get() > 10)

    tPool.close()
  }

  "ThreadPool" should "scale number of threads from 2 to 10" in new scenario {
    override def minThreads: Int = 2

    override def maxThreads: Int = 10

    override def ttl: FiniteDuration = inf

    List.range(0, 1000).foreach(i => tPool.execute(taskDelayed))
    Thread.sleep(3000)
    threadsArray.size shouldBe 10
    assert(atomicLong.get() > 100)
    tPool.close()
  }

  "ThreadPool" should "kill free threads instantly" in new scenario {
    override def minThreads: Int = 0

    override def maxThreads: Int = 10

    override def ttl: FiniteDuration = 0.seconds

    List.range(0, 500).foreach(i => tPool.execute(taskDelayed))
    Thread.sleep(3000)
    threadsArray.count(_.getState == State.TERMINATED) == 10
    assert(atomicLong.get() > 100)
    tPool.close()
  }

  "ThreadPool" should "kill free threads instantly and then recreate" in new scenario {
    override def minThreads: Int = 0

    override def maxThreads: Int = 3

    override def ttl: FiniteDuration = 0.seconds

    List.range(0, 200).foreach(i => tPool.execute(taskDelayed))
    Thread.sleep(3000)
    assert(threadsArray.count(_.getState == State.TERMINATED) == 3)

    List.range(0, 200).foreach(i => tPool.execute(taskDelayed))
    Thread.sleep(3000)
    assert(threadsArray.count(_.getState == State.TERMINATED) == 6)
    tPool.close()
  }

}
