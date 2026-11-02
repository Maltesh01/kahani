export type Profile = {
  id: number
  name: string
  headline: string
  city: string
  gender: string | null
  age: number | null
  relationshipStatus: string | null
  whatHereFor: string[]
  occupation: string | null
  nativeCity: string | null
  languages: string[]
  story: string
  interests: string[]
  interestNotes: string[]
  valuesNote: string | null
  matchPercent: number
  sharedInterests: string[]
}

export type Account = {
  profile: Profile
  gender: 'MAN' | 'WOMAN'
  accountStatus: 'ACTIVE' | 'SUBSCRIPTION_REQUIRED' | 'PENDING_VERIFICATION'
  verificationStatus: 'PENDING' | 'APPROVED' | 'REJECTED'
  instagramHandle: string
  instagramVerified: boolean
  voiceRecordingSubmitted: boolean
  subscriptionActive: boolean
  creditsBalance: number
  phoneNumber: string
  accountId: string
  admin: boolean
}

export type Connection = {
  requestId: number
  status: 'PENDING' | 'ACCEPTED'
  direction: 'INCOMING' | 'OUTGOING' | 'CONNECTED'
  createdAt: string
  introNote: string | null
  profile: Profile
}

export type ChatMessage = {
  id: number
  senderId: number
  body: string
  createdAt: string
}

export type InstagramShare = {
  senderId: number
  handle: string
  sharedAt: string
}

export type VerificationCase = {
  profileId: number
  name: string
  gender: 'MAN' | 'WOMAN'
  headline: string
  city: string
  story: string
  instagramHandle: string
  voiceRecordingSubmitted: boolean
  accountId: string
  relationshipStatus: string
  dateOfBirth: string | null
  whatHereFor: string
  occupation: string
  nativeCity: string
  languages: string
  interests: string
  interestNotes: string
  valuesNote: string
}

export type CreditPurchase = {
  id: number
  profileId: number
  accountId: string
  name: string
  packageCode: string
  amountRupees: number
  credits: number
  paymentReference: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  createdAt: string
}

export type CreditPurchaseReviewResult = {
  purchaseId: number
  accountId: string
  creditsAdded: number
  creditBalance: number
  subscriptionActive: boolean
}

export type AdminUser = {
  profileId: number
  name: string
  accountId: string
  email: string
  phoneNumber: string
  gender: string
  city: string
  verificationStatus: string
  suspended: boolean
  subscriptionActive: boolean
  creditsBalance: number
}

export type AdminAnalytics = {
  totalUsers: number
  approvedProfiles: number
  pendingVerifications: number
  suspendedUsers: number
  activeSubscriptions: number
  pendingPayments: number
  approvedPayments: number
  acceptedConnections: number
  pendingConnections: number
  totalMessages: number
  pendingReports: number
  approvedPaymentRevenueRupees: number
}

export type SafetyReportCategory =
  | 'HARASSMENT'
  | 'INAPPROPRIATE_CONTENT'
  | 'FAKE_PROFILE'
  | 'SCAM_OR_SPAM'
  | 'OTHER'

export type SafetyReport = {
  id: number
  reporterProfileId: number
  reporterName: string
  reporterAccountId: string
  reportedProfileId: number
  reportedName: string
  reportedAccountId: string
  category: SafetyReportCategory
  details: string
  status: 'PENDING' | 'RESOLVED' | 'DISMISSED'
  createdAt: string
  reviewedAt: string | null
}
