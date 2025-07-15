use std::fmt;
use thiserror::Error;

use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Callsign {
    original: String,
    normalized: String,
}

#[derive(Error, Debug, Clone, PartialEq)]
pub enum CallsignError {
    #[error("Invalid character '{0}' in callsign")]
    InvalidCharacter(char),

    #[error("Callsign too long: {actual} characters (max {max})")]
    TooLong { actual: usize, max: usize },

    #[error("Callsign cannot be empty")]
    Empty,
}

impl Callsign {
    const MAX_LENGTH: usize = 20;

    pub fn new(s: &str) -> Result<Self, CallsignError> {
        if s.is_empty() {
            return Err(CallsignError::Empty);
        }

        if s.len() > Self::MAX_LENGTH {
            return Err(CallsignError::TooLong {
                actual: s.len(),
                max: Self::MAX_LENGTH,
            });
        }

        if let Some(invalid_char) = s.chars().find(|&c| !Self::is_valid_char(c)) {
            return Err(CallsignError::InvalidCharacter(invalid_char));
        }

        Ok(Callsign {
            original: s.to_string(),
            normalized: s.to_ascii_lowercase(),
        })
    }

    fn is_valid_char(c: char) -> bool {
        c.is_ascii_alphanumeric() || c == '_'
    }

    /// Returns the original case-preserved string
    pub fn as_str(&self) -> &str {
        &self.original
    }

    /// Returns the normalized lowercase string (useful for debugging/logging)
    pub fn normalized(&self) -> &str {
        &self.normalized
    }
}

impl PartialEq for Callsign {
    fn eq(&self, other: &Self) -> bool {
        self.normalized == other.normalized
    }
}

impl Eq for Callsign {}

impl std::hash::Hash for Callsign {
    fn hash<H: std::hash::Hasher>(&self, state: &mut H) {
        self.normalized.hash(state);
    }
}

impl AsRef<str> for Callsign {
    fn as_ref(&self) -> &str {
        &self.original
    }
}

impl fmt::Display for Callsign {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{}", self.original)
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn should_create_callsign_when_input_contains_only_valid_characters() {
        let valid_cases = vec![
            "A",
            "ABC123",
            "test_call",
            "Test_Call_123",
            "a1b2c3",
            "UPPERCASE",
            "lowercase",
            "Mixed_Case_123",
            "1234567890",
        ];

        for case in valid_cases {
            let result = Callsign::new(&case);
            assert!(result.is_ok(), "Expected '{}' to be valid", case);
            assert_eq!(result.unwrap().as_str(), case);
        }
    }

    #[test]
    fn should_preserve_original_case() {
        let callsign = Callsign::new("TeSt_CaLl_123").unwrap();

        assert_eq!(callsign.as_str(), "TeSt_CaLl_123");
        assert_eq!(callsign.normalized(), "test_call_123");
    }

    #[test]
    fn should_be_case_insensitive_for_equality() {
        let callsign1 = Callsign::new("Test_Call").unwrap();
        let callsign2 = Callsign::new("TEST_CALL").unwrap();
        let callsign3 = Callsign::new("test_call").unwrap();
        let callsign4 = Callsign::new("TeSt_CaLl").unwrap();

        assert_eq!(callsign1, callsign2);
        assert_eq!(callsign1, callsign3);
        assert_eq!(callsign1, callsign4);
        assert_eq!(callsign2, callsign3);
        assert_eq!(callsign2, callsign4);
        assert_eq!(callsign3, callsign4);
    }

    #[test]
    fn should_be_case_insensitive_for_hashing() {
        use std::collections::HashMap;

        let callsign1 = Callsign::new("Test_Call").unwrap();
        let callsign2 = Callsign::new("TEST_CALL").unwrap();
        let callsign3 = Callsign::new("test_call").unwrap();

        let mut map = HashMap::new();
        map.insert(callsign1, "value1");

        // All case variations should map to the same entry
        assert_eq!(map.get(&callsign2), Some(&"value1"));
        assert_eq!(map.get(&callsign3), Some(&"value1"));

        // Updating with different case should replace the same entry
        map.insert(callsign2, "value2");
        assert_eq!(map.len(), 1);
        assert_eq!(map.get(&callsign3), Some(&"value2"));
    }

    #[test]
    fn should_display_original_case() {
        let callsign = Callsign::new("TeSt_CaLl_123").unwrap();

        assert_eq!(format!("{}", callsign), "TeSt_CaLl_123");
    }

    #[test]
    fn should_return_original_case_through_as_ref() {
        let callsign = Callsign::new("TeSt_CaLl_123").unwrap();
        let as_ref: &str = callsign.as_ref();

        assert_eq!(as_ref, "TeSt_CaLl_123");
    }

    #[test]
    fn should_return_empty_error_when_input_is_empty_string() {
        let result = Callsign::new("");

        assert!(matches!(result, Err(CallsignError::Empty)));
        assert_eq!(result.unwrap_err().to_string(), "Callsign cannot be empty");
    }

    #[test]
    fn should_return_too_long_error_when_input_exceeds_20_characters() {
        let too_long = "A".repeat(21);

        let result = Callsign::new(&too_long);

        assert!(matches!(
            result,
            Err(CallsignError::TooLong {
                actual: 21,
                max: 20
            })
        ));
        assert_eq!(
            result.unwrap_err().to_string(),
            "Callsign too long: 21 characters (max 20)"
        );
    }

    #[test]
    fn should_return_invalid_character_error_when_input_contains_special_characters() {
        let invalid_cases = vec![
            ("hello-world", '-'),
            ("test@example", '@'),
            ("call sign", ' '),
            ("test.call", '.'),
            ("call#sign", '#'),
            ("café", 'é'),
        ];

        for (input, expected_char) in invalid_cases {
            let result = Callsign::new(input);

            assert!(
                matches!(result, Err(CallsignError::InvalidCharacter(ch)) if ch == expected_char),
                "Expected InvalidCharacter('{}') for input '{}'",
                expected_char,
                input
            );
        }
    }

    #[test]
    fn should_create_callsign_when_input_is_exactly_20_characters() {
        let exactly_20_chars = "A".repeat(20);

        let result = Callsign::new(&exactly_20_chars);

        assert!(result.is_ok());
        assert_eq!(result.unwrap().as_str(), exactly_20_chars);
    }

    #[test]
    fn should_report_first_invalid_character_when_multiple_invalid_characters_exist() {
        let result = Callsign::new("Test@Call#Sign");

        assert!(matches!(result, Err(CallsignError::InvalidCharacter('@'))));
    }

    #[test]
    fn should_validate_empty_before_length_before_characters() {
        let result = Callsign::new("");
        assert!(matches!(result, Err(CallsignError::Empty)));

        let too_long_with_invalid = format!("{}@", "A".repeat(21));
        let result = Callsign::new(&too_long_with_invalid);
        assert!(matches!(result, Err(CallsignError::TooLong { .. })));
    }

    #[test]
    fn should_be_cloneable_with_same_behavior() {
        let original = Callsign::new("TeSt_CaLl").unwrap();
        let cloned = original.clone();

        assert_eq!(original, cloned);
        assert_eq!(original.as_str(), cloned.as_str());
        assert_eq!(original.normalized(), cloned.normalized());
    }

    #[test]
    fn should_have_cloneable_and_comparable_errors() {
        let error1 = CallsignError::InvalidCharacter('$');
        let error2 = error1.clone();
        let error3 = CallsignError::TooLong {
            actual: 25,
            max: 20,
        };

        assert_eq!(error1, error2);
        assert_ne!(error1, error3);
    }
}
