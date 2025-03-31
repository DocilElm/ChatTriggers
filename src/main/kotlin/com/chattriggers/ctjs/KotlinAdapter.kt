package com.chattriggers.ctjs

import net.minecraftforge.fml.common.FMLModContainer
import net.minecraftforge.fml.common.ILanguageAdapter
import net.minecraftforge.fml.common.ModContainer
import net.minecraftforge.fml.relauncher.Side
import java.lang.reflect.Field
import java.lang.reflect.Method

class KotlinAdapter : ILanguageAdapter {
    override fun getNewInstance(p0: FMLModContainer, p1: Class<*>, p2: ClassLoader, p3: Method?): Any {
        return p1.kotlin.objectInstance ?: p1.newInstance()
    }

    override fun supportsStatics(): Boolean {
        return false
    }

    override fun setProxy(p0: Field, p1: Class<*>, p2: Any) {
        p0.set(p1.kotlin.objectInstance, p2)
    }

    override fun setInternalProxies(p0: ModContainer?, p1: Side?, p2: ClassLoader?) {}
}