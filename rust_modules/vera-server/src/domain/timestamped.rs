use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};

use crate::domain::clock::{Clock, SystemClock};

#[derive(Debug, Clone, Copy, Serialize, Deserialize)]
pub struct Timestamped<T> {
    pub data: T,
    pub timestamp: DateTime<Utc>,
}

impl<T> Timestamped<T> {
    pub fn new(data: T, timestamp: DateTime<Utc>) -> Self {
        Self { data, timestamp }
    }

    pub fn now(data: T) -> Self {
        Self::with_clock(data, &SystemClock)
    }

    pub fn with_clock(data: T, clock: &impl Clock) -> Self {
        Self {
            data,
            timestamp: clock.now(),
        }
    }

    pub fn map<U>(self, f: impl FnOnce(T) -> U) -> Timestamped<U> {
        Timestamped {
            data: f(self.data),
            timestamp: self.timestamp,
        }
    }

    pub fn try_map<U, E>(self, f: impl FnOnce(T) -> Result<U, E>) -> Result<Timestamped<U>, E> {
        Ok(Timestamped {
            data: f(self.data)?,
            timestamp: self.timestamp,
        })
    }
}
