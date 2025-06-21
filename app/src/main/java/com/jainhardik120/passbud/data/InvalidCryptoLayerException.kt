package com.jainhardik120.passbud.data

import com.jainhardik120.passbud.data.crypto.ValidationResult

class InvalidCryptoLayerException(validationResult: ValidationResult) : Exception()