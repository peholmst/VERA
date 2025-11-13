#include "Alert.hpp"

ActiveAlerts::ActiveAlerts(std::chrono::seconds timeout, std::chrono::seconds flashDuration) : timeout(timeout), flashDuration(flashDuration)
{
}

void ActiveAlerts::Push(const Alert alert)
{
    std::scoped_lock lock(mutex);
    alerts.push_front(alert);
    lastFlash = Timestamp::clock::now();
}

std::optional<Alert> ActiveAlerts::Poll()
{
    std::scoped_lock lock(mutex);
    const auto now = Timestamp::clock::now();

    // Remove expired alerts
    alerts.erase(
        std::remove_if(alerts.begin(), alerts.end(),
                       [&](const Alert &a)
                       {
                           return (now - a.timestamp) > timeout;
                       }),
        alerts.end());

    if (alerts.empty())
    {
        return std::nullopt;
    }

    // Check if it's time to rotate (flash)
    if (now - lastFlash > flashDuration && alerts.size() > 1)
    {
        // Move the front alert to the back
        auto front = alerts.front();
        alerts.pop_front();
        alerts.push_back(front);
        lastFlash = now;
    }

    // Return the current top alert
    return alerts.front();
}