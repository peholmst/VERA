use std::fmt;

#[derive(Debug, Clone, PartialEq, Hash)]
pub struct PhoneNumber(String);

impl PhoneNumber {
    pub fn from_string(s: &str) -> Result<Self, String> {
        let mut sanitized = s.to_string();
        sanitized.retain(|c| !r#"()- "#.contains(c));
        if sanitized.is_empty() || sanitized.len() > 15 {
            return Err("Incorrect length".into());
        }

        let numbers_only = sanitized.strip_prefix('+').unwrap_or(&sanitized);
        if !numbers_only.chars().all(|c| c.is_ascii_digit()) {
            return Err("Invalid characters".into());
        }

        Ok(PhoneNumber(sanitized))
    }
}

impl fmt::Display for PhoneNumber {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{}", self.0)
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn parses_and_sanitizes_phone_numbers() {
        let local = PhoneNumber::from_string("(040) 123 4567");
        assert!(local.is_ok());
        assert_eq!(local.unwrap().to_string(), "0401234567");

        let international = PhoneNumber::from_string("+358-40-123-4567");
        assert!(international.is_ok());
        assert_eq!(international.unwrap().to_string(), "+358401234567");
    }

    #[test]
    fn phone_number_has_equality_and_clone_behavior() {
        let a = PhoneNumber::from_string("0401234567");
        let b = a.clone();
        assert_eq!(a, b);
    }
}
