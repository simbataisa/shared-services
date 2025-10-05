import { useEffect, useState } from 'react'
import api from '../lib/api'

type UserGroup = {
  userGroupId?: number
  id?: number
  name: string
  description?: string
  memberCount?: number
}

export default function UserGroups() {
  const [groups, setGroups] = useState<UserGroup[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const load = async () => {
      setLoading(true)
      setError(null)
      try {
        const { data } = await api.get('/user-groups')
        const list = data?.data?.content || data?.data || []
        setGroups(list)
      } catch (e) {
        setError('Failed to load groups')
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [])

  const createGroup = async () => {
    try {
      setLoading(true)
      const name = `Group ${new Date().toLocaleTimeString()}`
      await api.post('/user-groups', { name, description: 'Created from UI' })
      const { data } = await api.get('/user-groups')
      const list = data?.data?.content || data?.data || []
      setGroups(list)
    } catch (e) {
      setError('Failed to create group')
    } finally {
      setLoading(false)
    }
  }

  if (loading) return <p>Loading...</p>
  if (error) return <p className="text-red-600">{error}</p>

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold">Permission Groups</h2>
        <button className="btn" onClick={createGroup}>New Group</button>
      </div>
      <div className="bg-white rounded-lg shadow divide-y">
        {groups.length === 0 && (
          <p className="p-4 text-gray-500">No groups yet.</p>
        )}
        {groups.map((g) => (
          <div key={g.id || g.userGroupId} className="p-4 flex items-center justify-between">
            <div>
              <p className="font-medium">{g.name}</p>
              {g.description && (
                <p className="text-sm text-gray-600">{g.description}</p>
              )}
            </div>
            <span className="text-sm text-gray-500">Members: {g.memberCount ?? '-'}</span>
          </div>
        ))}
      </div>
    </div>
  )
}