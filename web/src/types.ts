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
