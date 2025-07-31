use serde::{Deserialize, Serialize};

use crate::domain::timestamped::Timestamped;

#[derive(Debug, Clone, Copy, Serialize, Deserialize)]
pub struct Staffing {
    officers: u8,
    sub_officers: u8,
    crew: u8,
}

pub type TimestampedStaffing = Timestamped<Staffing>;

impl Staffing {
    pub fn new(officers: u8, sub_officers: u8, crew: u8) -> Self {
        Self {
            officers,
            sub_officers,
            crew,
        }
    }

    pub fn officers(&self) -> u8 {
        self.officers
    }

    pub fn sub_officers(&self) -> u8 {
        self.sub_officers
    }

    pub fn crew(&self) -> u8 {
        self.crew
    }
}
