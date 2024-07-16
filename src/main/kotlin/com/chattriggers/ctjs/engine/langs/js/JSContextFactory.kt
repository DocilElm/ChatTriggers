package com.chattriggers.ctjs.engine.langs.js

import net.minecraft.launchwrapper.Launch
import org.mozilla.javascript.Context
import org.mozilla.javascript.Context.EMIT_DEBUG_OUTPUT
import org.mozilla.javascript.Context.FEATURE_LOCATION_INFORMATION_IN_ERROR
import org.mozilla.javascript.ContextFactory
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.WrapFactory
import java.io.File
import java.net.URL
import java.net.URLClassLoader

object JSContextFactory : ContextFactory() {
    private var classLoader: ModifiedURLClassLoader? = null
    var wrapCollection = true
    var optimize = true

    fun addAllURLs(urls: List<URL>) = classLoader!!.addAllURLs(urls)

    fun closeLoader() = classLoader!!.close()

    fun setupLoader() {
        classLoader = ModifiedURLClassLoader()
    }

    override fun onContextCreated(cx: Context) {
        super.onContextCreated(cx)

        if (Launch.blackboard.getOrDefault("fml.deobfuscatedEnvironment", false) as Boolean) cx.debugOutputPath = File(".", "DEBUG")
        cx.applicationClassLoader = classLoader
        cx.optimizationLevel = if (optimize) 9 else 0
        cx.languageVersion = Context.VERSION_ES6
        cx.errorReporter = JSErrorReporter(JSLoader.console.writer.printWriter)
        cx.wrapFactory = object : WrapFactory() {
            override fun wrap(cx: Context?, scope: Scriptable?, obj: Any?, staticType: Class<*>?): Any? {
                if (wrapCollection && obj is Collection<*>) {
                    // TODO: Wrapping an array is somewhat slow
                    return super.wrap(cx, scope, obj.toTypedArray(), staticType)
                }

                return super.wrap(cx, scope, obj, staticType)
            }
        }.apply { isJavaPrimitiveWrap = false }
    }

    override fun hasFeature(cx: Context?, featureIndex: Int): Boolean {
        when (featureIndex) {
            FEATURE_LOCATION_INFORMATION_IN_ERROR -> return true
            EMIT_DEBUG_OUTPUT -> return Launch.blackboard.getOrDefault("fml.deobfuscatedEnvironment", false) as Boolean
        }

        return super.hasFeature(cx, featureIndex)
    }

    private class ModifiedURLClassLoader : URLClassLoader(arrayOf(), javaClass.classLoader) {
        val sources = mutableListOf<URL>()

        fun addAllURLs(urls: List<URL>) {
            (urls - sources).forEach(::addURL)
        }

        public override fun addURL(url: URL) {
            super.addURL(url)
            sources.add(url)
        }
    }
}
