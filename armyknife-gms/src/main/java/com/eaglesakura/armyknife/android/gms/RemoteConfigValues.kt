package com.eaglesakura.armyknife.android.gms

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlin.reflect.KProperty

@Deprecated("unsupported, not use this.")
object RemoteConfigValues {
    private val config: FirebaseRemoteConfig?
        get() = try {
            FirebaseRemoteConfig.getInstance()
        } catch (err: Throwable) {
            null
        }

    private val defMap = mutableMapOf<String, Any>()

    /**
     * This method return getProperty for RemoteConfig with Any types.
     * Supported types)
     *  String, Boolean, Int, Long, Float, Double
     * example)
     *
     * val serverEndpoint:String by getRemoteProperty("server_endpoint", "http://example.com")
     */
    fun <T> getRemoteProperty(key: String, defValue: T): IConfig<T> {
        val defValueObj: Any = defValue as Any
        val result: Any = when (defValue.javaClass.kotlin) {
            Boolean::class -> BooleanConfig(key, defValueObj as Boolean)
            Int::class -> IntConfig(key, defValueObj as Int)
            Long::class -> LongConfig(key, defValueObj as Long)
            Float::class -> FloatConfig(key, defValueObj as Float)
            Double::class -> DoubleConfig(key, defValueObj as Double)
            String::class -> StringConfig(key, defValueObj as String)
            else -> throw IllegalArgumentException("Class<${defValue.javaClass}> not supported")
        }

        @Suppress("UNCHECKED_CAST")
        return result as IConfig<T>
    }

    /**
     * This method returns getProperty for RemoteConfig with object-converter.
     * "converter" in argument should be support converting from string-value to  "T" object.
     */
    fun <T> getRemoteProperty(key: String, converter: (value: String) -> T): IConfig<T> {
        return object : IConfig<T> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): T {
                return converter(config?.getString(key) ?: "")
            }
        }
    }

    /**
     * Firebase Remote Configにデフォルト値を認識させる.
     */
    fun activate() {
        config?.setDefaults(defMap)
    }

    interface IConfig<T> {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T
    }

    private class IntConfig(private val key: String, defValue: Int) : IConfig<Int> {
        init {
            defMap[key] = defValue
        }

        override operator fun getValue(thisRef: Any?, property: KProperty<*>): Int {
            return config?.getLong(key)?.toInt() ?: defMap[key] as Int
        }
    }

    private class LongConfig(private val key: String, defValue: Long) : IConfig<Long> {
        init {
            defMap[key] = defValue
        }

        override operator fun getValue(thisRef: Any?, property: KProperty<*>): Long {
            return config?.getLong(key) ?: defMap[key] as Long
        }
    }

    private class BooleanConfig(private val key: String, defValue: Boolean) : IConfig<Boolean> {
        init {
            defMap[key] = defValue
        }

        override operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
            return config?.getBoolean(key) ?: defMap[key] as Boolean
        }
    }

    private class FloatConfig(private val key: String, defValue: Float) : IConfig<Float> {
        init {
            defMap[key] = defValue
        }

        override operator fun getValue(thisRef: Any?, property: KProperty<*>): Float {
            return config?.getDouble(key)?.toFloat() ?: defMap[key] as Float
        }
    }

    private class DoubleConfig(private val key: String, defValue: Double) : IConfig<Double> {
        init {
            defMap[key] = defValue
        }

        override operator fun getValue(thisRef: Any?, property: KProperty<*>): Double {
            return config?.getDouble(key) ?: defMap[key] as Double
        }
    }

    private class StringConfig(private val key: String, defValue: String) : IConfig<String> {
        init {
            defMap[key] = defValue
        }

        override operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
            return config?.getString(key) ?: defMap[key] as String
        }
    }
}