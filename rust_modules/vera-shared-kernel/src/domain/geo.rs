use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};
use thiserror::Error;

use crate::domain::{Clock, SystemClock, Timestamped};

#[derive(Debug, Clone, Copy, Serialize, Deserialize)]
pub struct Coordinate {
    latitude: f64,
    longitude: f64,
}

#[derive(Error, Debug)]
pub enum CoordinateError {
    #[error("Latitude must be between -90 and 90 degrees")]
    InvalidLatitude(f64),
    #[error("Longitude must be between -180 and 180 degrees")]
    InvalidLongitude(f64),
}

impl Coordinate {
    pub fn new(latitude: f64, longitude: f64) -> Result<Self, CoordinateError> {
        if latitude < -90.0 || latitude > 90.0 {
            return Err(CoordinateError::InvalidLatitude(latitude));
        }
        if longitude < -180.0 || longitude > 180.0 {
            return Err(CoordinateError::InvalidLongitude(longitude));
        }
        Ok(Coordinate {
            latitude,
            longitude,
        })
    }

    pub fn latitude(&self) -> f64 {
        self.latitude
    }

    pub fn longitude(&self) -> f64 {
        self.longitude
    }
}

pub type TimestampedCoordinate = Timestamped<Coordinate>;

impl Timestamped<Coordinate> {
    pub fn try_new(
        latitude: f64,
        longitude: f64,
        timestamp: DateTime<Utc>,
    ) -> Result<Self, CoordinateError> {
        let coordinate = Coordinate::new(latitude, longitude)?;
        Ok(Self::new(coordinate, timestamp))
    }

    pub fn try_now(latitude: f64, longitude: f64) -> Result<Self, CoordinateError> {
        Self::try_with_clock(latitude, longitude, &SystemClock)
    }

    pub fn try_with_clock(
        latitude: f64,
        longitude: f64,
        clock: &impl Clock,
    ) -> Result<Self, CoordinateError> {
        let coordinate = Coordinate::new(latitude, longitude)?;
        Ok(Self::with_clock(coordinate, clock))
    }
}
