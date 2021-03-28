import jsonFetcher from '../utils/jsonFetcher'
import useSuspense from '../utils/useSuspense'
import { useParams } from 'react-router-dom'
import { onError } from '../utils/suspenseDefaults'

import Loading from '../components/Loading'
import ErrorComp from '../components/ErrorComp'
import Selector from '../components/Selector'
import MemberTable from '../components/MemberTable'
import ProjectSummary from '../components/ProjectSummary'
import Button from '../components/Button'

import styles from '../css/Project.module.css'

export interface IMember {
  id: string
  username: string
  displayName: string
  role: string
}

export interface IProjectData {
  id: string
  name: string
  members: IMember[]
  numBranches: number
  numCommits: number
  repoSize: number
  createdAt: number
}

const Project = () => {
  const { id } = useParams<{ id: string }>()

  const { Suspense, data: project, error } = useSuspense<IProjectData, Error>(
    (setData, setError) => {
      jsonFetcher<IProjectData>(`/api/project/${id}`)
        .then(data => setData(data))
        .catch(onError(setError))
    }
  )

  return (
    <Suspense
      fallback={<Loading message="Getting project details.." />}
      error={<ErrorComp message={error?.message ?? 'Unknown Error'} />}
    >
      <div className={styles.container}>
        <h1 className={styles.header}>{project?.name}</h1>
        <Selector tabHeaders={['Summary', 'Members']}>
          <div className={styles.summaryContainer}>
            <ProjectSummary project={project} />
          </div>
          <div className={styles.memberContainer}>
            <MemberTable projectId={id} projectName={project?.name ?? ''} />
            <Button
              message="Member to Committer Resolution"
              destPath={`/project/${id}/memberResolution`}
            />
          </div>
        </Selector>
      </div>
    </Suspense>
  )
}

export default Project
