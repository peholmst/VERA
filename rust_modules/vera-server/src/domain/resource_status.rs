use serde::{Deserialize, Serialize};

use crate::domain::timestamped::Timestamped;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum ResourceStatus {
    AvailableAtStation,
    AvailableOnRadio,
    Assigned,
    Dispatched,
    EnRoute,
    OnScene,
    Unavailable,
}

pub type TimestampedResourceStatus = Timestamped<ResourceStatus>;
