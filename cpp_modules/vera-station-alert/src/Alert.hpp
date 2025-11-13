#ifndef __ALERT_HPP__
#define __ALERT_HPP__

#include <algorithm>
#include <chrono>
#include <deque>
#include <mutex>
#include <optional>
#include <string>
#include <vector>

using Timestamp = std::chrono::time_point<std::chrono::utc_clock>;

struct Alert
{
    Timestamp timestamp;
    std::string assignment_code;
    std::string assignment_urgency;
    std::string municipality;
    std::string address;
    std::string details;
    std::vector<std::string> units;
};

class ActiveAlerts
{
public:
    ActiveAlerts(std::chrono::seconds timeout, std::chrono::seconds flashDuration);
    void Push(const Alert alert);
    std::optional<Alert> Poll();

private:
    std::chrono::seconds timeout;
    std::chrono::seconds flashDuration;

    std::deque<Alert> alerts;
    Timestamp lastFlash{Timestamp::clock::now()};
    mutable std::mutex mutex;
};

#endif