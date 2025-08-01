#[macro_export]
macro_rules! define_id_type {
    ($name:ident) => {
        #[derive(Debug, Clone, Copy, PartialEq, Eq, Hash)]
        pub struct $name(pub uuid::Uuid);

        impl $name {
            pub fn new() -> Self {
                Self(uuid::Uuid::new_v4())
            }

            pub fn from_uuid(uuid: uuid::Uuid) -> Self {
                Self(uuid)
            }

            pub fn as_uuid(&self) -> &uuid::Uuid {
                &self.0
            }
        }

        impl std::fmt::Display for $name {
            fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
                write!(f, "{}", self.0)
            }
        }

        impl std::str::FromStr for $name {
            type Err = uuid::Error;

            fn from_str(s: &str) -> Result<Self, Self::Err> {
                uuid::Uuid::from_str(s).map(Self)
            }
        }

        impl From<uuid::Uuid> for $name {
            fn from(uuid: uuid::Uuid) -> Self {
                Self(uuid)
            }
        }

        impl From<$name> for uuid::Uuid {
            fn from(id: $name) -> Self {
                id.0
            }
        }
    };
}
