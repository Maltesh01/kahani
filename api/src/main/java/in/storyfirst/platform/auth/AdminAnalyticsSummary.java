package in.storyfirst.platform.auth;

public record AdminAnalyticsSummary(
        long totalUsers,
        long approvedProfiles,
        long pendingVerifications,
        long suspendedUsers,
        long activeSubscriptions,
        long pendingPayments,
        long approvedPayments,
        long acceptedConnections,
        long pendingConnections,
        long totalMessages,
        long pendingReports,
        long approvedPaymentRevenueRupees) {
}
