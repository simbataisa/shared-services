import React from 'react'
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Checkbox } from '@/components/ui/checkbox'
import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'

interface Role {
  id: number
  name: string
  description: string
  moduleId: number
  moduleName: string
}

interface Module {
  id: number
  name: string
  description: string
}

interface UserGroup {
  userGroupId: number
  name: string
  description: string
  memberCount: number
}

interface RoleAssignmentDialogProps {
  isOpen: boolean
  onOpenChange: (open: boolean) => void
  selectedGroup: UserGroup | null
  modules: Module[]
  roles: Role[]
  selectedModule: string
  selectedRoles: number[]
  roleLoading: boolean
  onModuleChange: (moduleId: string) => void
  onRoleToggle: (roleId: number) => void
  onAssignRoles: () => void
}

const RoleAssignmentDialog: React.FC<RoleAssignmentDialogProps> = ({
  isOpen,
  onOpenChange,
  selectedGroup,
  modules,
  roles,
  selectedModule,
  selectedRoles,
  roleLoading,
  onModuleChange,
  onRoleToggle,
  onAssignRoles
}) => {
  // Filter roles by selected module
  const filteredRoles = selectedModule 
    ? roles.filter(role => role.moduleId === parseInt(selectedModule))
    : []

  return (
    <Dialog open={isOpen} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Manage Roles for {selectedGroup?.name}</DialogTitle>
          <DialogDescription>
            Assign roles to this user group for specific modules.
          </DialogDescription>
        </DialogHeader>
        <div className="grid gap-4 py-4">
          <div className="grid gap-2">
            <Label htmlFor="module-select">Module</Label>
            <Select value={selectedModule} onValueChange={onModuleChange}>
              <SelectTrigger>
                <SelectValue placeholder="Select a module" />
              </SelectTrigger>
              <SelectContent>
                {modules.map((module) => (
                  <SelectItem key={module.id} value={module.id.toString()}>
                    {module.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          
          {selectedModule && (
            <div className="grid gap-2">
              <Label>Roles</Label>
              <div className="space-y-2 max-h-48 overflow-y-auto">
                {filteredRoles.map((role) => (
                  <div key={role.id} className="flex items-center space-x-2">
                    <Checkbox
                      id={`role-${role.id}`}
                      checked={selectedRoles.includes(role.id)}
                      onCheckedChange={() => onRoleToggle(role.id)}
                    />
                    <Label htmlFor={`role-${role.id}`} className="text-sm">
                      <div className="font-medium">{role.name}</div>
                      {role.description && (
                        <div className="text-xs text-muted-foreground">{role.description}</div>
                      )}
                    </Label>
                  </div>
                ))}
                {filteredRoles.length === 0 && (
                  <div className="text-sm text-muted-foreground">
                    No roles available for this module
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            Cancel
          </Button>
          <Button 
            onClick={onAssignRoles} 
            disabled={!selectedModule || selectedRoles.length === 0 || roleLoading}
          >
            {roleLoading ? 'Assigning...' : 'Assign Roles'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

export default RoleAssignmentDialog