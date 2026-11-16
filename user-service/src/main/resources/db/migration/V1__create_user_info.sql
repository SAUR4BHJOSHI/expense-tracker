CREATE TABLE user_info (
  user_id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  first_name       VARCHAR(255),
  last_name       VARCHAR(255),
  email         VARCHAR(255) NOT NULL UNIQUE,
  profile_status        VARCHAR(50) NOT NULL DEFAULT 'INCOMPLETE',
  profile_picture_url VARCHAR(255),
  preferred_currency VARCHAR(10) NOT NULL DEFAULT 'INR',
  created_at    TIMESTAMP NOT NULL DEFAULT now(),
  updated_at    TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_user_info_email ON user_info(email);
CREATE INDEX idx_user_info_user_id ON user_info(user_id);
