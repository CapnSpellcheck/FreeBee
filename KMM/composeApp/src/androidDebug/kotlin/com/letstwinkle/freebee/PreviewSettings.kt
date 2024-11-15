package com.letstwinkle.freebee

import com.russhwolf.settings.Settings

class PreviewSettings : Settings {
   private val booleans = mutableMapOf<String, Boolean>()
   private val doubles = mutableMapOf<String, Double>()
   private val ints = mutableMapOf<String, Int>()
   private val floats = mutableMapOf<String, Float>()
   private val strings = mutableMapOf<String, String>()
   private val longs = mutableMapOf<String, Long>()
   
   override val keys: Set<String>
      get() = booleans.keys + doubles.keys + ints.keys + floats.keys + strings.keys + longs.keys
   override val size: Int
      get() = keys.size
   
   override fun clear() {
      booleans.clear()
      doubles.clear()
      ints.clear()
      floats.clear()
      strings.clear()
      longs.clear()
   }
   
   override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
      getBooleanOrNull(key) ?: defaultValue
   
   override fun getBooleanOrNull(key: String): Boolean? = booleans.get(key)
   
   override fun getDouble(key: String, defaultValue: Double): Double =
      getDoubleOrNull(key) ?: defaultValue
   
   
   override fun getDoubleOrNull(key: String): Double? = doubles.get(key)
   
   override fun getFloat(key: String, defaultValue: Float): Float =
      getFloatOrNull(key) ?: defaultValue
   
   override fun getFloatOrNull(key: String): Float? = floats.get(key)
   
   override fun getInt(key: String, defaultValue: Int): Int {
      return getIntOrNull(key) ?: defaultValue
   }
   
   override fun getIntOrNull(key: String): Int? = ints.get(key)
   
   override fun getLong(key: String, defaultValue: Long): Long =
      getLongOrNull(key) ?: defaultValue
   
   override fun getLongOrNull(key: String): Long? = longs.get(key)
   
   override fun getString(key: String, defaultValue: String): String =
      getStringOrNull(key) ?: defaultValue
   
   override fun getStringOrNull(key: String): String? = strings.get(key)
   
   override fun hasKey(key: String): Boolean = keys.contains(key)
   
   override fun putBoolean(key: String, value: Boolean) {
      booleans.put(key, value)
   }
   
   override fun putDouble(key: String, value: Double) {
      doubles.put(key, value)
   }
   
   override fun putFloat(key: String, value: Float) {
      floats.put(key, value)
   }
   
   override fun putInt(key: String, value: Int) {
      ints.put(key,value)
   }
   
   override fun putLong(key: String, value: Long) {
      longs.put(key, value)
   }
   
   override fun putString(key: String, value: String) {
      strings.put(key, value)
   }
   
   override fun remove(key: String) {
      booleans.remove(key)
      doubles.remove(key)
      ints.remove(key)
      floats.remove(key)
      strings.remove(key)
      longs.remove(key)      
   }
}
