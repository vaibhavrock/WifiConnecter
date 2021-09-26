package app.poc.wifi

object Common {
    fun checkWifiType(capabilities: String): String {
        if (capabilities.toUpperCase().contains("WEP")) {
            return "WEP"
        } else if (capabilities.toUpperCase().contains("WPA")) {
            return "WPA"
        } else {
            return "OPEN"
        }
    }
}