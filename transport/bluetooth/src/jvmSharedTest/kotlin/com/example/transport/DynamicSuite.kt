package com.example.transport

import org.junit.internal.AssumptionViolatedException
import org.junit.internal.runners.model.EachTestNotifier
import org.junit.runner.Description
import org.junit.runner.Runner
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener
import org.junit.runner.notification.RunNotifier
import org.junit.runner.notification.StoppedByUserException
import org.junit.runners.Suite
import org.junit.runners.model.RunnerBuilder
import java.lang.reflect.Method
import java.util.concurrent.atomic.AtomicBoolean


class DynamicSuite(setupClass: Class<*>?, builder: RunnerBuilder) :
    Suite(setupClass, builder.runners(setupClass, testFromSuiteMethod(setupClass!!))) {
    private var runChildNotifer: RunNotifier? = null
    private val failListener: FailFastListener? = null

    companion object {
        @Throws(Throwable::class)
        fun testFromSuiteMethod(klass: Class<*>): Array<Class<*>> {
            val suiteMethod: Method
            val children: Array<Class<*>>?
            try {
                suiteMethod = klass.getMethod("suite")
                if (!java.lang.reflect.Modifier.isStatic(suiteMethod.modifiers)) {
                    throw java.lang.Exception(klass.name + ".suite() must be static")
                }
                children = suiteMethod.invoke(null) as Array<Class<*>> // static method
            } catch (e: java.lang.reflect.InvocationTargetException) {
                throw e.cause!!
            }
            return children
        }
    }

    override fun runChild(runner: Runner, notifier: RunNotifier) {
        val listener = FailFastListener(notifier)
        if (failListener?.failed?.get() == true) {
            notifier.fireTestIgnored(runner.description)
            return
        }
        notifier.addListener(listener)
        runner.run(notifier)
    }

    override fun run(notifier: RunNotifier) {
        val testNotifier = EachTestNotifier(
            notifier,
            description
        )
        testNotifier.fireTestSuiteStarted()
        try {
            val statement = classBlock(notifier)
            statement.evaluate()
        } catch (e: AssumptionViolatedException) {
            testNotifier.addFailedAssumption(e)
        } catch (e: StoppedByUserException) {
            if (failListener?.failed?.get() == true) {
                throw failListener.exception!!
            }
            throw e
        } catch (e: Throwable) {
            testNotifier.addFailure(e)
        } finally {
            testNotifier.fireTestSuiteFinished()
        }
    }

    class FailFastListener(private val notifier: RunNotifier) : RunListener() {
        val failed = AtomicBoolean(false)
        var exception: Throwable? = null
            private set

        override fun testFailure(failure: Failure) {
            exception = failure.exception
            println("${failure.testHeader} failed: $exception")
            exception!!.printStackTrace()
            failed.compareAndSet(false, true)
        }

        override fun testFinished(description: Description) {
            println("${description.displayName} finished")
            if (failed.get()) {
                notifier.pleaseStop()
            }
        }

        override fun testAssumptionFailure(failure: Failure) {
            failed.compareAndSet(false, true)
            println("${failure.testHeader} assumption failed")
            exception = failure.exception
        }
    }
}
